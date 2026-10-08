package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.entity.Lead;
import com.caprae.leadintelligence.scoring.LeadPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AIOutreachPromptBuilderTest {

    private AIOutreachPromptBuilder promptBuilder;
    private Lead fullLead;
    private Lead bareLead;

    @BeforeEach
    void setUp() {
        promptBuilder = new AIOutreachPromptBuilder();

        fullLead = new Lead();
        fullLead.setId(1L);
        fullLead.setCompanyName("Nova Technology");
        fullLead.setWebsite("https://novatech.io");
        fullLead.setIndustry("Technology");
        fullLead.setRevenue(new BigDecimal("12500000"));
        fullLead.setEmployeeCount(95);
        fullLead.setLocation("California");
        fullLead.setGrowthSignal("Hiring");
        fullLead.setDecisionMakerName("Emily Davis");
        fullLead.setDecisionMakerTitle("VP of Engineering");
        fullLead.setDecisionMakerLinkedIn("https://linkedin.com/in/emilydavis");
        fullLead.setScore(87);
        fullLead.setPriority(LeadPriority.HIGH);

        bareLead = new Lead();
        bareLead.setId(2L);
        bareLead.setCompanyName("Barebones LLC");
    }

    // ─── System prompt ────────────────────────────────────────────────────────

    @Test
    void buildSystemPrompt_containsAllProhibitedHallucinations() {
        String prompt = promptBuilder.buildSystemPrompt();
        assertNotNull(prompt);

        assertTrue(prompt.contains("previous conversation"),        "should forbid: previous conversation");
        assertTrue(prompt.contains("ai adoption") || prompt.contains("AI adoption"), "should forbid: ai adoption");
        assertTrue(prompt.contains("ai initiatives") || prompt.contains("AI initiatives"), "should forbid: ai initiatives");
        assertTrue(prompt.contains("ai technologies") || prompt.contains("AI technologies"), "should forbid: ai technologies");
        assertTrue(prompt.contains("engineering expansion") || prompt.contains("team expansion"), "should forbid: engineering expansion");
        assertTrue(prompt.contains("growth needs") || prompt.contains("business needs"), "should forbid: growth needs");
        assertTrue(prompt.contains("buying intent"), "should forbid: buying intent");
        assertTrue(prompt.contains("technology solutions") || prompt.contains("technology stack"), "should forbid: technology solutions/stack");
        assertTrue(prompt.contains("actively hiring"), "should forbid: actively hiring");
    }

    @Test
    void buildSystemPrompt_restrictsToPreviousConversation() {
        String prompt = promptBuilder.buildSystemPrompt();
        assertTrue(prompt.contains("previous conversation"),
                "System prompt must explicitly prohibit 'previous conversation'");
    }

    @Test
    void buildSystemPrompt_limitsOllamaToThreeFields() {
        String prompt = promptBuilder.buildSystemPrompt();
        assertTrue(prompt.contains("talkingPoints"),         "System prompt must mention talkingPoints");
        assertTrue(prompt.contains("personalizationAngle"),  "System prompt must mention personalizationAngle");
        assertTrue(prompt.contains("suggestedEmailOpening"), "System prompt must mention suggestedEmailOpening");
    }

    @Test
    void buildSystemPrompt_prohibitsCommunicationAndProcessProblems() {
        String prompt = promptBuilder.buildSystemPrompt();
        assertTrue(prompt.contains("communication problems") || prompt.contains("communication challenges"),
                "System prompt must forbid communication problem claims");
        assertTrue(prompt.contains("workflow issues") || prompt.contains("process problems"),
                "System prompt must forbid workflow/process problem claims");
        assertTrue(prompt.contains("operational challenges") || prompt.contains("operational bottlenecks"),
                "System prompt must forbid operational challenge claims");
        assertTrue(prompt.contains("integration problems") || prompt.contains("collaboration issues"),
                "System prompt must forbid integration/collaboration problem claims");
    }

    @Test
    void buildSystemPrompt_prohibitsTechnologyAdoptionAssumptions() {
        String prompt = promptBuilder.buildSystemPrompt();
        assertTrue(prompt.contains("technology adoption") || prompt.contains("digital transformation"),
                "System prompt must forbid technology adoption assumptions");
        assertTrue(prompt.contains("technology problems") || prompt.contains("technology challenges"),
                "System prompt must forbid technology problem assumptions");
        assertTrue(prompt.contains("tool adoption") || prompt.contains("software adoption"),
                "System prompt must forbid tool/software adoption assumptions");
    }

    @Test
    void buildSystemPrompt_prohibitsBusinessAssumptions() {
        String prompt = promptBuilder.buildSystemPrompt();
        assertTrue(prompt.contains("pain points"),  "System prompt must forbid 'pain points'");
        assertTrue(prompt.contains("business challenges") || prompt.contains("business problems"),
                "System prompt must forbid business challenge assumptions");
        assertTrue(prompt.contains("growth strategy") || prompt.contains("scaling challenges"),
                "System prompt must forbid growth/scaling assumptions");
        assertTrue(prompt.contains("recent investments") || prompt.contains("future plans"),
                "System prompt must forbid invented investment or plan claims");
    }

    @Test
    void buildSystemPrompt_containsQuestionRule() {
        String prompt = promptBuilder.buildSystemPrompt();
        assertTrue(prompt.contains("QUESTION RULE"),
                "System prompt must contain a QUESTION RULE section");
        assertTrue(prompt.contains("CORRECT") || prompt.contains("WRONG"),
                "System prompt must show a correct vs wrong framing example");
    }

    // ─── Fact sheet ───────────────────────────────────────────────────────────

    @Test
    void buildFactSheet_includesAllLeadFieldsForFullLead() {
        String factSheet = promptBuilder.buildFactSheet(fullLead);

        assertTrue(factSheet.contains("Company Name: Nova Technology"));
        assertTrue(factSheet.contains("Industry: Technology"));
        assertTrue(factSheet.contains("Revenue: $12,500,000"));
        assertTrue(factSheet.contains("Employee Count: 95"));
        assertTrue(factSheet.contains("Location: California"));
        assertTrue(factSheet.contains("Growth Signal: Hiring"));
        assertTrue(factSheet.contains("Decision Maker Name: Emily Davis"));
        assertTrue(factSheet.contains("Decision Maker Title: VP of Engineering"));
        assertTrue(factSheet.contains("Decision Maker LinkedIn: https://linkedin.com/in/emilydavis"));
        assertTrue(factSheet.contains("Deterministic Lead Score: 87/100"));
    }

    @Test
    void buildFactSheet_preservesLinkedInUrlWhenPresent() {
        String factSheet = promptBuilder.buildFactSheet(fullLead);
        assertTrue(factSheet.contains("Decision Maker LinkedIn: https://linkedin.com/in/emilydavis"),
                "Fact sheet must preserve the LinkedIn URL as-is");
        assertFalse(factSheet.contains("Decision Maker LinkedIn: Unavailable"),
                "Fact sheet must NOT report LinkedIn as Unavailable when a URL is present");
    }

    @Test
    void buildFactSheet_reportsLinkedInUnavailableWhenNull() {
        String factSheet = promptBuilder.buildFactSheet(bareLead);
        assertTrue(factSheet.contains("Decision Maker LinkedIn: Unavailable"),
                "Fact sheet must report Unavailable when LinkedIn is null");
    }

    @Test
    void buildFactSheet_handlesNullFieldsGracefully() {
        String factSheet = promptBuilder.buildFactSheet(bareLead);
        assertTrue(factSheet.contains("Revenue: Unknown / Not Disclosed"));
        assertTrue(factSheet.contains("Employee Count: Unknown"));
        assertTrue(factSheet.contains("Decision Maker Name: Unknown"));
        assertTrue(factSheet.contains("Growth Signal: None Recorded"));
    }

    // ─── User prompt ──────────────────────────────────────────────────────────

    @Test
    void buildUserPrompt_containsFactSheetAndJsonInstruction() {
        String prompt = promptBuilder.buildUserPrompt(fullLead);
        assertTrue(prompt.contains("GROUNDED LEAD FACT SHEET"));
        assertTrue(prompt.contains("talkingPoints"));
        assertTrue(prompt.contains("personalizationAngle"));
        assertTrue(prompt.contains("suggestedEmailOpening"));
    }

    @Test
    void buildUserPrompt_doesNotAskForSummaryChannelOrRiskNotes() {
        // These fields are now deterministic — the LLM must NOT be asked to generate them
        String prompt = promptBuilder.buildUserPrompt(fullLead);
        assertFalse(prompt.contains("\"summary\""),           "LLM prompt must NOT ask for summary");
        assertFalse(prompt.contains("\"recommendedChannel\""), "LLM prompt must NOT ask for recommendedChannel");
        assertFalse(prompt.contains("\"riskNotes\""),          "LLM prompt must NOT ask for riskNotes");
    }

    @Test
    void buildUserPrompt_containsProhibitedReminderBlock() {
        String prompt = promptBuilder.buildUserPrompt(fullLead);
        assertTrue(prompt.contains("PROHIBITED"),
                "User prompt must include a PROHIBITED reminder block");
        assertTrue(prompt.contains("communication problems") || prompt.contains("pain points"),
                "User prompt PROHIBITED block must reference communication/pain-point terms");
        assertTrue(prompt.contains("previous conversation"),
                "User prompt PROHIBITED block must list 'previous conversation'");
    }
}
