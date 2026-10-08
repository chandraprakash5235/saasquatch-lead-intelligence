package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.dto.LeadImportResponse;
import com.caprae.leadintelligence.dto.LeadResponse;
import com.caprae.leadintelligence.entity.Lead;
import com.caprae.leadintelligence.exception.InvalidCsvException;
import com.caprae.leadintelligence.exception.LeadNotFoundException;
import com.caprae.leadintelligence.repository.LeadRepository;
import com.caprae.leadintelligence.scoring.LeadPriority;
import com.caprae.leadintelligence.scoring.LeadScoreResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    private LeadScoringService leadScoringService;
    private LeadService leadService;

    @BeforeEach
    void setUp() {
        leadScoringService = new LeadScoringService();
        leadService = new LeadService(leadRepository, leadScoringService);
    }

    @Test
    void shouldCalculateDataCompletenessCorrectly() {
        Lead lead = new Lead();
        lead.setCompanyName("Acme Corp");
        lead.setIndustry("Technology");
        lead.setRevenue(new BigDecimal("5000000"));
        lead.setEmployeeCount(50);
        lead.setLocation("California");

        int score = leadService.calculateDataCompleteness(lead);
        assertThat(score).isEqualTo(50);
    }

    @Test
    void shouldSaveNewLeadWithCompletenessAndScore() {
        Lead lead = new Lead();
        lead.setCompanyName("Innovate Inc");
        lead.setIndustry("Technology");
        lead.setRevenue(new BigDecimal("10000000"));

        given(leadRepository.existsByCompanyNameIgnoreCase("Innovate Inc")).willReturn(false);
        given(leadRepository.save(any(Lead.class))).willAnswer(invocation -> {
            Lead saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        LeadResponse response = leadService.saveLead(lead);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.score()).isGreaterThan(0);
        assertThat(response.priority()).isNotNull();
    }

    @Test
    void shouldGetLeadScoreResult() {
        Lead lead = new Lead();
        lead.setId(1L);
        lead.setCompanyName("Tech Corp");
        lead.setIndustry("Technology");
        lead.setDataCompleteness(80);

        given(leadRepository.findById(1L)).willReturn(Optional.of(lead));

        LeadScoreResult scoreResult = leadService.getLeadScore(1L);

        assertThat(scoreResult.companyName()).isEqualTo("Tech Corp");
        assertThat(scoreResult.totalScore()).isGreaterThan(0);
        assertThat(scoreResult.reasons()).isNotEmpty();
    }

    @Test
    void shouldImportValidCsvAndCalculateScores() {
        String csvContent = "company_name,industry,revenue,employees,location,growth_signal,decision_maker\n" +
                "ABC Manufacturing,Manufacturing,8200000,72,Texas,Hiring,John Smith\n" +
                "XYZ Services,Services,5100000,48,Florida,Expansion,Sarah Jones\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "leads.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        given(leadRepository.existsByCompanyNameIgnoreCase(any())).willReturn(false);
        given(leadRepository.save(any(Lead.class))).willAnswer(inv -> inv.getArgument(0));

        LeadImportResponse response = leadService.importCsv(file);

        assertThat(response.totalRows()).isEqualTo(2);
        assertThat(response.successfulRows()).isEqualTo(2);
        assertThat(response.failedRows()).isEqualTo(0);
    }
}
