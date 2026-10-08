package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.dto.OutreachBriefResponse;
import com.caprae.leadintelligence.entity.Lead;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AIOutreachValidatorTest {

    private Lead fullLead;   // LinkedIn present
    private Lead bareLead;   // LinkedIn absent

    @BeforeEach
    void setUp() {
        fullLead = new Lead();
        fullLead.setId(1L);
        fullLead.setCompanyName("Nova Technology");
        fullLead.setIndustry("Technology");
        fullLead.setRevenue(new BigDecimal("12500000"));
        fullLead.setEmployeeCount(95);
        fullLead.setLocation("California");
        fullLead.setGrowthSignal("Hiring");
        fullLead.setDecisionMakerName("Emily Davis");
        fullLead.setDecisionMakerTitle("VP of Engineering");
        fullLead.setDecisionMakerLinkedIn("https://linkedin.com/in/emilydavis");

        bareLead = new Lead();
        bareLead.setId(2L);
        bareLead.setCompanyName("Barebones LLC");
        bareLead.setDecisionMakerLinkedIn(null);
    }

    // ─── hasValidLinkedIn ────────────────────────────────────────────────────

    @Test
    void hasValidLinkedIn_returnsTrueWhenUrlPresent() {
        assertTrue(AIOutreachValidator.hasValidLinkedIn(fullLead));
    }

    @Test
    void hasValidLinkedIn_returnsFalseWhenNull() {
        assertFalse(AIOutreachValidator.hasValidLinkedIn(bareLead));
    }

    @Test
    void hasValidLinkedIn_returnsFalseWhenEmpty() {
        bareLead.setDecisionMakerLinkedIn("   ");
        assertFalse(AIOutreachValidator.hasValidLinkedIn(bareLead));
    }

    // ─── buildDeterministicSummary ───────────────────────────────────────────

    @Test
    void buildDeterministicSummary_containsOnlyLeadFields() {
        String summary = AIOutreachValidator.buildDeterministicSummary(fullLead);

        assertTrue(summary.contains("Nova Technology"));
        assertTrue(summary.contains("Technology"));
        assertTrue(summary.contains("California"));
        assertTrue(summary.contains("$12,500,000"));
        assertTrue(summary.contains("95 employees"));
        assertTrue(summary.contains("The recorded growth signal is Hiring"));
        assertTrue(summary.contains("Emily Davis"));
        assertTrue(summary.contains("VP of Engineering"));
    }

    @Test
    void buildDeterministicSummary_doesNotUseProhibitedPhrases() {
        String summary = AIOutreachValidator.buildDeterministicSummary(fullLead).toLowerCase();

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

    @Test
    void buildDeterministicSummary_handlesNullFieldsGracefully() {
        String summary = AIOutreachValidator.buildDeterministicSummary(bareLead);
        assertTrue(summary.contains("Barebones LLC"));
        assertTrue(summary.contains("revenue not disclosed"));
        assertFalse(summary.contains("null"), "summary must never contain raw 'null'");
    }

    // ─── buildDeterministicChannel ───────────────────────────────────────────

    @Test
    void buildDeterministicChannel_linkedInPresent_returnsLinkedInInMail() {
        assertEquals("LinkedIn InMail", AIOutreachValidator.buildDeterministicChannel(fullLead));
    }

    @Test
    void buildDeterministicChannel_noLinkedIn_returnsEmail() {
        assertEquals("Email", AIOutreachValidator.buildDeterministicChannel(bareLead));
    }

    // ─── buildDeterministicRiskNotes ─────────────────────────────────────────

    @Test
    void buildDeterministicRiskNotes_allFieldsPresent_returnsCleanNote() {
        String riskNotes = AIOutreachValidator.buildDeterministicRiskNotes(fullLead);
        // When all primary fields are populated the note should be positive
        assertTrue(riskNotes.toLowerCase().contains("all primary"),
                "RiskNotes must say all primary fields are populated when data is complete");
    }

    @Test
    void buildDeterministicRiskNotes_missingLinkedIn_mentionsGap() {
        String riskNotes = AIOutreachValidator.buildDeterministicRiskNotes(bareLead);
        assertTrue(riskNotes.toLowerCase().contains("linkedin"),
                "RiskNotes must mention the LinkedIn gap when URL is missing");
    }

    @Test
    void buildDeterministicRiskNotes_doesNotIncorrectlyReportLinkedInMissing() {
        String riskNotes = AIOutreachValidator.buildDeterministicRiskNotes(fullLead);
        assertFalse(riskNotes.toLowerCase().contains("linkedin"),
                "RiskNotes must NOT report LinkedIn as missing when URL is present");
    }

    // ─── buildDeterministicTalkingPoints ─────────────────────────────────────

    @Test
    void buildDeterministicTalkingPoints_containsGrowthSignalAndIndustryAndDm() {
        List<String> points = AIOutreachValidator.buildDeterministicTalkingPoints(fullLead);

        assertTrue(points.stream().anyMatch(p -> p.contains("Hiring")),
                "Talking points must reference the recorded growth signal");
        assertTrue(points.stream().anyMatch(p -> p.contains("Technology")),
                "Talking points must reference the industry");
        assertTrue(points.stream().anyMatch(p -> p.contains("Emily Davis")),
                "Talking points must reference the decision maker name");
    }

    @Test
    void buildDeterministicTalkingPoints_doesNotUseProhibitedPhrases() {
        List<String> points = AIOutreachValidator.buildDeterministicTalkingPoints(fullLead);
        String combined = String.join(" ", points).toLowerCase();

        assertFalse(combined.contains("actively hiring"),       "must not say 'actively hiring'");
        assertFalse(combined.contains("ai initiatives"),        "must not mention AI initiatives");
        assertFalse(combined.contains("ai technologies"),       "must not mention AI technologies");
        assertFalse(combined.contains("engineering expansion"), "must not mention engineering expansion");
        assertFalse(combined.contains("growth needs"),         "must not mention growth needs");
        assertFalse(combined.contains("previous conversation"), "must not mention previous conversation");
        assertFalse(combined.contains("interested in ai"),     "must not mention interest in AI");
        assertFalse(combined.contains("technology solutions"), "must not mention technology solutions");
        assertFalse(combined.contains("business needs"),       "must not mention business needs");
        assertFalse(combined.contains("key decision maker"),   "must not say 'key decision maker'");
    }

    // ─── validateAiFields ────────────────────────────────────────────────────

    @Test
    void validateAiFields_cleanOutput_passes() {
        List<String> points = List.of(
                "The recorded growth signal is Hiring.",
                "The company operates in the Technology industry.",
                "The recorded decision maker is Emily Davis, VP of Engineering."
        );
        String angle   = "Consider asking whether priorities at Nova Technology align with your offering.";
        String opening = "Hi Emily, I came across Nova Technology in the Technology space and wanted to reach out.";

        assertTrue(AIOutreachValidator.validateAiFields(points, angle, opening, 1L));
    }

    @Test
    void validateAiFields_previousConversation_fails() {
        List<String> points = List.of("Following up on our previous conversation.");
        assertFalse(AIOutreachValidator.validateAiFields(points, "", "", 1L));
    }

    @Test
    void validateAiFields_aiInitiatives_fails() {
        List<String> points = List.of("Nova Technology is exploring AI initiatives.");
        assertFalse(AIOutreachValidator.validateAiFields(points, "", "", 1L));
    }

    @Test
    void validateAiFields_aiTechnologies_fails() {
        List<String> points = List.of("They are adopting AI technologies.");
        assertFalse(AIOutreachValidator.validateAiFields(points, "", "", 1L));
    }

    @Test
    void validateAiFields_engineeringExpansion_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "engineering expansion plans", "", 1L));
    }

    @Test
    void validateAiFields_growthNeeds_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "address growth needs", "", 1L));
    }

    @Test
    void validateAiFields_activelyHiring_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of("Nova is actively hiring."), "", "", 1L));
    }

    @Test
    void validateAiFields_interestedInAi_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "", "Emily is interested in AI.", 1L));
    }

    @Test
    void validateAiFields_keyDecisionMaker_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of("Emily Davis is the key decision maker."), "", "", 1L));
    }

    @Test
    void validateAiFields_technologySolutions_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "offer technology solutions", "", 1L));
    }

    @Test
    void validateAiFields_businessNeeds_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "", "address your business needs", 1L));
    }

    // ─── NEW: communication / process / workflow prohibitions ────────────────

    @Test
    void validateAiFields_communicationProblems_fails() {
        List<String> points = List.of("We can solve your communication problems.");
        assertFalse(AIOutreachValidator.validateAiFields(points, "", "", 1L),
                "communication problems must be prohibited");
    }

    @Test
    void validateAiFields_communicationChallenges_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "address communication challenges", "", 1L),
                "communication challenges must be prohibited");
    }

    @Test
    void validateAiFields_workflowIssues_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of("Streamline your workflow issues."), "", "", 1L),
                "workflow issues must be prohibited");
    }

    @Test
    void validateAiFields_operationalChallenges_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "we understand your operational challenges", "", 1L),
                "operational challenges must be prohibited");
    }

    @Test
    void validateAiFields_integrationProblems_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of("We solve integration problems."), "", "", 1L),
                "integration problems must be prohibited");
    }

    @Test
    void validateAiFields_productivityProblems_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "", "improve productivity problems", 1L),
                "productivity problems must be prohibited");
    }

    @Test
    void validateAiFields_efficiencyIssues_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "resolve efficiency issues", "", 1L),
                "efficiency issues must be prohibited");
    }

    // ─── NEW: technology adoption prohibitions ────────────────────────────────

    @Test
    void validateAiFields_technologyAdoption_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of("Supporting technology adoption."), "", "", 1L),
                "technology adoption must be prohibited");
    }

    @Test
    void validateAiFields_technologyProblems_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "solve technology problems", "", 1L),
                "technology problems must be prohibited");
    }

    @Test
    void validateAiFields_digitalTransformation_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "", "your digital transformation journey", 1L),
                "digital transformation must be prohibited");
    }

    // ─── NEW: business assumption prohibitions ────────────────────────────────

    @Test
    void validateAiFields_painPoints_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of("Addressing your pain points."), "", "", 1L),
                "pain points must be prohibited");
    }

    @Test
    void validateAiFields_futureGrowthStrategy_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "to support your growth strategy", "", 1L),
                "growth strategy must be prohibited");
    }

    @Test
    void validateAiFields_businessChallenges_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "", "tackle business challenges", 1L),
                "business challenges must be prohibited");
    }

    @Test
    void validateAiFields_scalingChallenges_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of("help with scaling challenges"), "", "", 1L),
                "scaling challenges must be prohibited");
    }

    @Test
    void validateAiFields_stakeholderPriorities_fails() {
        assertFalse(AIOutreachValidator.validateAiFields(List.of(), "based on stakeholder priorities", "", 1L),
                "stakeholder priorities must be prohibited");
    }

    // ─── Verify clean neutral questions still PASS ────────────────────────────

    @Test
    void validateAiFields_neutralQuestionForm_passes() {
        List<String> points = List.of(
                "The recorded growth signal is Hiring.",
                "The company operates in the Technology industry.",
                "Would it be useful to explore whether our offering fits your current priorities?"
        );
        String angle   = "Would it be helpful to understand how companies in the Technology sector use our platform?";
        String opening = "Hi Emily, I came across Nova Technology in the Technology space and wanted to connect.";

        assertTrue(AIOutreachValidator.validateAiFields(points, angle, opening, 1L),
                "Neutral questions framed as questions and grounded in fact-sheet data should pass");
    }
}
