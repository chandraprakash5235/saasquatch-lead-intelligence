package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.config.OllamaProperties;
import com.caprae.leadintelligence.dto.OutreachBriefResponse;
import com.caprae.leadintelligence.entity.Lead;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Hybrid-architecture service tests.
 *
 * Contract:
 *  - summary, recommendedChannel, riskNotes, leadId, companyName → always deterministic from Java
 *  - talkingPoints, personalizationAngle, suggestedEmailOpening → from Ollama when clean, else deterministic fallback
 */
@ExtendWith(MockitoExtension.class)
class AIOutreachServiceTest {

    @Mock private LeadService leadService;
    @Mock private HttpClient httpClient;
    @Mock private HttpResponse<String> httpResponse;

    private AIOutreachService aiOutreachService;
    private Lead sampleLead;

    @BeforeEach
    void setUp() {
        OllamaProperties ollamaProperties = new OllamaProperties();
        ollamaProperties.setBaseUrl("http://localhost:11434");
        ollamaProperties.setModel("qwen2.5:3b");
        ollamaProperties.setTimeoutSeconds(120);

        aiOutreachService = new AIOutreachService(
                leadService,
                ollamaProperties,
                new AIOutreachPromptBuilder(),
                new ObjectMapper(),
                httpClient);

        sampleLead = new Lead();
        sampleLead.setId(1L);
        sampleLead.setCompanyName("Acme Corp");
        sampleLead.setIndustry("Technology");
        sampleLead.setRevenue(new BigDecimal("10000000"));
        sampleLead.setEmployeeCount(150);
        sampleLead.setLocation("New York");
        sampleLead.setGrowthSignal("Hiring");
        sampleLead.setDecisionMakerName("Jane Doe");
        sampleLead.setDecisionMakerTitle("CTO");
        sampleLead.setDecisionMakerLinkedIn("https://linkedin.com/in/janedoe");
    }

    // ── Deterministic fields are always correct regardless of Ollama output ──

    @Test
    void deterministicFields_areAlwaysSetByJava_notByOllama() throws Exception {
        when(leadService.getLeadEntityById(1L)).thenReturn(sampleLead);

        // Ollama returns valid clean talking points only
        String ollamaBody = ollamaBodyWithContent("""
                {"talkingPoints":["The recorded growth signal is Hiring.","The company operates in the Technology industry."],"personalizationAngle":"Consider whether your offering is relevant to the CTO role.","suggestedEmailOpening":"Hi Jane, I came across Acme Corp in the Technology sector and wanted to reach out."}
                """);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(ollamaBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        OutreachBriefResponse result = aiOutreachService.generateOutreachBrief(1L);

        // Deterministic fields must be set from Lead data
        assertEquals(1L, result.getLeadId());
        assertEquals("Acme Corp", result.getCompanyName());

        // Summary must be Java-generated (contains field values, not LLM phrases)
        assertTrue(result.getSummary().contains("Acme Corp"),                        "summary must contain company name");
        assertTrue(result.getSummary().contains("Technology"),                       "summary must contain industry");
        assertTrue(result.getSummary().contains("New York"),                         "summary must contain location");
        assertTrue(result.getSummary().contains("$10,000,000"),                      "summary must contain revenue");
        assertTrue(result.getSummary().contains("150 employees"),                    "summary must contain employee count");
        assertTrue(result.getSummary().contains("The recorded growth signal is Hiring"), "summary must contain growth signal");
        assertTrue(result.getSummary().contains("Jane Doe"),                         "summary must contain decision maker name");

        // Channel must be deterministic from LinkedIn presence
        assertEquals("LinkedIn InMail", result.getRecommendedChannel());

        // Risk notes must be deterministic (all fields present → clean note)
        assertTrue(result.getRiskNotes().toLowerCase().contains("all primary"),
                "riskNotes must confirm all fields are present");
    }

    @Test
    void deterministicSummary_doesNotContainProhibitedPhrases() throws Exception {
        when(leadService.getLeadEntityById(1L)).thenReturn(sampleLead);
        when(httpClient.send(any(), any())).thenThrow(new IOException("offline"));

        OutreachBriefResponse result = aiOutreachService.generateOutreachBrief(1L);
        String summary = result.getSummary().toLowerCase();

        assertFalse(summary.contains("actively hiring"),       "summary must not say 'actively hiring'");
        assertFalse(summary.contains("key decision maker"),    "summary must not say 'key decision maker'");
        assertFalse(summary.contains("ai initiatives"),        "summary must not mention AI initiatives");
        assertFalse(summary.contains("ai technologies"),       "summary must not mention AI technologies");
        assertFalse(summary.contains("engineering expansion"), "summary must not mention engineering expansion");
        assertFalse(summary.contains("growth needs"),          "summary must not mention growth needs");
        assertFalse(summary.contains("business needs"),        "summary must not mention business needs");
        assertFalse(summary.contains("previous conversation"), "summary must not mention previous conversation");
        assertFalse(summary.contains("interested in ai"),      "summary must not mention interest in AI");
        assertFalse(summary.contains("technology solutions"),  "summary must not mention technology solutions");
    }

    // ── When Ollama is unavailable → deterministic fallback for AI fields ────

    @Test
    void ollamaUnavailable_deterministicFieldsStillCorrect() throws Exception {
        when(leadService.getLeadEntityById(1L)).thenReturn(sampleLead);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection refused"));

        OutreachBriefResponse result = aiOutreachService.generateOutreachBrief(1L);

        assertNotNull(result);
        assertEquals(1L, result.getLeadId());
        assertEquals("Acme Corp", result.getCompanyName());
        assertTrue(result.getSummary().contains("Acme Corp"),    "fallback summary must contain company name");
        assertEquals("LinkedIn InMail", result.getRecommendedChannel(), "channel must be deterministic from LinkedIn URL");
        assertTrue(result.getRiskNotes().toLowerCase().contains("all primary"),
                "fallback riskNotes must be clean when LinkedIn is present");
    }

    @Test
    void ollamaUnavailable_deterministicTalkingPointsDoNotHallucinate() throws Exception {
        when(leadService.getLeadEntityById(1L)).thenReturn(sampleLead);
        when(httpClient.send(any(), any())).thenThrow(new IOException("offline"));

        OutreachBriefResponse result = aiOutreachService.generateOutreachBrief(1L);
        String allContent = String.join(" ", result.getTalkingPoints()).toLowerCase()
                + " " + result.getPersonalizationAngle().toLowerCase()
                + " " + result.getSuggestedEmailOpening().toLowerCase();

        assertFalse(allContent.contains("ai initiatives"),        "must not mention AI initiatives");
        assertFalse(allContent.contains("ai technologies"),       "must not mention AI technologies");
        assertFalse(allContent.contains("engineering expansion"), "must not mention engineering expansion");
        assertFalse(allContent.contains("growth needs"),          "must not mention growth needs");
        assertFalse(allContent.contains("previous conversation"), "must not mention previous conversation");
        assertFalse(allContent.contains("actively hiring"),       "must not say 'actively hiring'");
        assertFalse(allContent.contains("interested in ai"),      "must not mention interest in AI");
        assertFalse(allContent.contains("technology solutions"),  "must not mention technology solutions");
        assertFalse(allContent.contains("business needs"),        "must not mention business needs");
        assertFalse(allContent.contains("key decision maker"),    "must not say 'key decision maker'");
    }

    // ── When Ollama returns hallucinated AI fields → fallback replaces them ──

    @Test
    void hallucinatedOllamaAiFields_areReplacedByDeterministicFallback() throws Exception {
        when(leadService.getLeadEntityById(1L)).thenReturn(sampleLead);

        // Talking points contain prohibited phrases
        String hallucinatedBody = ollamaBodyWithContent("""
                {"talkingPoints":["following up on our previous conversation","Acme Corp is interested in AI initiatives."],"personalizationAngle":"Jane is keen on AI technologies.","suggestedEmailOpening":"As discussed previously, we think Acme Corp has engineering expansion needs."}
                """);

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(hallucinatedBody);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        OutreachBriefResponse result = aiOutreachService.generateOutreachBrief(1L);

        // Deterministic fields must be untouched
        assertEquals("Acme Corp", result.getCompanyName());
        assertEquals("LinkedIn InMail", result.getRecommendedChannel());
        assertTrue(result.getSummary().contains("Acme Corp"));

        // AI fields must have been replaced by safe deterministic alternatives
        String allAiContent = String.join(" ", result.getTalkingPoints()).toLowerCase()
                + " " + result.getPersonalizationAngle().toLowerCase()
                + " " + result.getSuggestedEmailOpening().toLowerCase();

        assertFalse(allAiContent.contains("previous conversation"), "hallucinated 'previous conversation' must be removed");
        assertFalse(allAiContent.contains("interested in ai"),      "hallucinated AI interest must be removed");
        assertFalse(allAiContent.contains("ai initiatives"),        "hallucinated AI initiatives must be removed");
        assertFalse(allAiContent.contains("engineering expansion"), "hallucinated expansion must be removed");
        assertFalse(allAiContent.contains("as discussed"),         "hallucinated 'as discussed' must be removed");

        // Fallback talking points must reference actual lead fields
        String talkingPointsCombined = String.join(" ", result.getTalkingPoints());
        assertTrue(talkingPointsCombined.contains("Hiring") || talkingPointsCombined.contains("Technology"),
                "Fallback talking points must reference actual lead fields");
    }

    // ── No OpenAI call ever ────────────────────────────────────────────────────

    @Test
    void generatesOutreach_callsOllamaNotOpenAi() throws Exception {
        when(leadService.getLeadEntityById(1L)).thenReturn(sampleLead);

        String ollamaBody = ollamaBodyWithContent("""
                {"talkingPoints":["The recorded growth signal is Hiring."],"personalizationAngle":"Consider asking whether your offering matches Jane Doe's current priorities.","suggestedEmailOpening":"Hi Jane, I came across Acme Corp in the Technology sector."}
                """);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(ollamaBody);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        when(httpClient.send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        aiOutreachService.generateOutreachBrief(1L);

        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("http://localhost:11434/api/chat", capturedRequest.uri().toString());
        assertFalse(capturedRequest.uri().toString().contains("openai.com"), "Must never call OpenAI");
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    /** Wraps inner JSON content in a valid Ollama /api/chat response envelope. */
    private String ollamaBodyWithContent(String innerJson) throws Exception {
        ObjectMapper om = new ObjectMapper();
        String escaped = innerJson.strip();
        // Build envelope manually to avoid double-serialisation issues
        return """
                {
                  "model": "qwen2.5:3b",
                  "message": {
                    "role": "assistant",
                    "content": %s
                  },
                  "done": true
                }
                """.formatted(om.writeValueAsString(escaped));
    }
}
