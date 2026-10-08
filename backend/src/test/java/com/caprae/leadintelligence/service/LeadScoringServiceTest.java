package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.entity.Lead;
import com.caprae.leadintelligence.scoring.LeadPriority;
import com.caprae.leadintelligence.scoring.LeadScoreResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LeadScoringServiceTest {

    private LeadScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new LeadScoringService();
    }

    @Test
    @DisplayName("Should score 30 points for preferred industry (e.g. Technology), 15 for known, 0 for missing")
    void testIcpFitScoring() {
        Lead techLead = new Lead();
        techLead.setIndustry("Technology");
        assertThat(scoringService.calculateScore(techLead).icpFitScore()).isEqualTo(30);

        Lead saasLead = new Lead();
        saasLead.setIndustry("SaaS");
        assertThat(scoringService.calculateScore(saasLead).icpFitScore()).isEqualTo(30);

        Lead knownLead = new Lead();
        knownLead.setIndustry("Agriculture");
        assertThat(scoringService.calculateScore(knownLead).icpFitScore()).isEqualTo(15);

        Lead missingLead = new Lead();
        missingLead.setIndustry(null);
        assertThat(scoringService.calculateScore(missingLead).icpFitScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should score revenue tiers accurately using BigDecimal")
    void testRevenueFitScoring() {
        Lead rev10M = new Lead();
        rev10M.setRevenue(new BigDecimal("12000000"));
        assertThat(scoringService.calculateScore(rev10M).revenueFitScore()).isEqualTo(20);

        Lead rev5M = new Lead();
        rev5M.setRevenue(new BigDecimal("6500000"));
        assertThat(scoringService.calculateScore(rev5M).revenueFitScore()).isEqualTo(15);

        Lead rev2M = new Lead();
        rev2M.setRevenue(new BigDecimal("3000000"));
        assertThat(scoringService.calculateScore(rev2M).revenueFitScore()).isEqualTo(10);

        Lead rev500K = new Lead();
        rev500K.setRevenue(new BigDecimal("750000"));
        assertThat(scoringService.calculateScore(rev500K).revenueFitScore()).isEqualTo(5);

        Lead revLow = new Lead();
        revLow.setRevenue(new BigDecimal("200000"));
        assertThat(scoringService.calculateScore(revLow).revenueFitScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should score employee tiers accurately")
    void testEmployeeFitScoring() {
        Lead emp100 = new Lead();
        emp100.setEmployeeCount(150);
        assertThat(scoringService.calculateScore(emp100).employeeFitScore()).isEqualTo(15);

        Lead emp50 = new Lead();
        emp50.setEmployeeCount(75);
        assertThat(scoringService.calculateScore(emp50).employeeFitScore()).isEqualTo(12);

        Lead emp20 = new Lead();
        emp20.setEmployeeCount(30);
        assertThat(scoringService.calculateScore(emp20).employeeFitScore()).isEqualTo(8);

        Lead emp10 = new Lead();
        emp10.setEmployeeCount(15);
        assertThat(scoringService.calculateScore(emp10).employeeFitScore()).isEqualTo(4);

        Lead empLow = new Lead();
        empLow.setEmployeeCount(5);
        assertThat(scoringService.calculateScore(empLow).employeeFitScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should score growth signals correctly and default unknown/none to 0")
    void testGrowthSignalScoring() {
        Lead hiring = new Lead();
        hiring.setGrowthSignal("Hiring");
        assertThat(scoringService.calculateScore(hiring).growthSignalScore()).isEqualTo(15);

        Lead expansion = new Lead();
        expansion.setGrowthSignal("Expansion");
        assertThat(scoringService.calculateScore(expansion).growthSignalScore()).isEqualTo(15);

        Lead funding = new Lead();
        funding.setGrowthSignal("Funding");
        assertThat(scoringService.calculateScore(funding).growthSignalScore()).isEqualTo(12);

        Lead partnership = new Lead();
        partnership.setGrowthSignal("Partnership");
        assertThat(scoringService.calculateScore(partnership).growthSignalScore()).isEqualTo(8);

        Lead none = new Lead();
        none.setGrowthSignal("None");
        assertThat(scoringService.calculateScore(none).growthSignalScore()).isEqualTo(0);

        Lead unknown = new Lead();
        unknown.setGrowthSignal("RandomSignal");
        assertThat(scoringService.calculateScore(unknown).growthSignalScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should score decision maker quality")
    void testDecisionMakerScoring() {
        Lead fullDm = new Lead();
        fullDm.setDecisionMakerName("John Smith");
        fullDm.setDecisionMakerTitle("CEO");
        fullDm.setDecisionMakerLinkedIn("https://linkedin.com/in/john");
        assertThat(scoringService.calculateScore(fullDm).decisionMakerScore()).isEqualTo(10);

        Lead nameTitle = new Lead();
        nameTitle.setDecisionMakerName("Jane Doe");
        nameTitle.setDecisionMakerTitle("VP Sales");
        assertThat(scoringService.calculateScore(nameTitle).decisionMakerScore()).isEqualTo(8);

        Lead nameOnly = new Lead();
        nameOnly.setDecisionMakerName("Mark Brown");
        assertThat(scoringService.calculateScore(nameOnly).decisionMakerScore()).isEqualTo(5);

        Lead noDm = new Lead();
        assertThat(scoringService.calculateScore(noDm).decisionMakerScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should map total score to correct priority classification")
    void testPriorityClassification() {
        assertThat(LeadPriority.fromScore(100)).isEqualTo(LeadPriority.HIGH);
        assertThat(LeadPriority.fromScore(80)).isEqualTo(LeadPriority.HIGH);
        assertThat(LeadPriority.fromScore(79)).isEqualTo(LeadPriority.MEDIUM);
        assertThat(LeadPriority.fromScore(60)).isEqualTo(LeadPriority.MEDIUM);
        assertThat(LeadPriority.fromScore(59)).isEqualTo(LeadPriority.LOW);
        assertThat(LeadPriority.fromScore(0)).isEqualTo(LeadPriority.LOW);
    }

    @Test
    @DisplayName("Should return a perfect score of 100 for a fully qualified lead")
    void testPerfectLeadScore() {
        Lead lead = new Lead();
        lead.setId(1L);
        lead.setCompanyName("Nova Technology");
        lead.setIndustry("Technology");                  // 30
        lead.setRevenue(new BigDecimal("15000000"));      // 20
        lead.setEmployeeCount(120);                      // 15
        lead.setGrowthSignal("Hiring");                   // 15
        lead.setDecisionMakerName("Emily Davis");        // 10
        lead.setDecisionMakerTitle("CTO");
        lead.setDecisionMakerLinkedIn("https://linkedin.com/in/emily");
        lead.setDataCompleteness(95);                     // 10

        LeadScoreResult result = scoringService.calculateScore(lead);

        assertThat(result.totalScore()).isEqualTo(100);
        assertThat(result.priority()).isEqualTo(LeadPriority.HIGH);
        assertThat(result.reasons()).hasSize(6);
        assertThat(result.reasons()).contains("Preferred industry: Technology", "Revenue is $10M+", "100+ employees");
    }

    @Test
    @DisplayName("Should return low score for a weak lead")
    void testWeakLeadScore() {
        Lead lead = new Lead();
        lead.setCompanyName("Bare Company");

        LeadScoreResult result = scoringService.calculateScore(lead);

        assertThat(result.totalScore()).isEqualTo(0);
        assertThat(result.priority()).isEqualTo(LeadPriority.LOW);
        assertThat(result.reasons()).isEmpty();
    }
}
