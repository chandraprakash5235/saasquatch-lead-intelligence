package com.caprae.leadintelligence.controller;

import com.caprae.leadintelligence.dto.LeadImportResponse;
import com.caprae.leadintelligence.dto.LeadResponse;
import com.caprae.leadintelligence.exception.LeadNotFoundException;
import com.caprae.leadintelligence.scoring.LeadPriority;
import com.caprae.leadintelligence.scoring.LeadScoreResult;
import com.caprae.leadintelligence.service.AIOutreachService;
import com.caprae.leadintelligence.service.LeadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeadController.class)
class LeadControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private LeadService leadService;

        @MockBean
        private AIOutreachService aiOutreachService;

        @Test
        void shouldReturnPaginatedLeadsWithScores() throws Exception {
                LeadResponse lead = new LeadResponse(
                                1L, "ABC Manufacturing", "https://abc.com", "Manufacturing",
                                new BigDecimal("8200000"), 72, "Texas", "Hiring",
                                "John Smith", "VP Sales", "https://linkedin.com/in/johnsmith",
                                80, 87, LeadPriority.HIGH, LocalDateTime.now(), LocalDateTime.now());

                PageImpl<LeadResponse> page = new PageImpl<>(List.of(lead));
                given(leadService.getLeads(any(), any(), any(), any(), any(), any(Pageable.class))).willReturn(page);

                mockMvc.perform(get("/api/leads")
                                .param("priority", "HIGH")
                                .param("minScore", "80")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].companyName").value("ABC Manufacturing"))
                                .andExpect(jsonPath("$.content[0].score").value(87))
                                .andExpect(jsonPath("$.content[0].priority").value("HIGH"));
        }

        @Test
        void shouldReturnDetailedLeadScore() throws Exception {
                LeadScoreResult scoreResult = new LeadScoreResult(
                                1L, "Nova Technology", 87, 30, 15, 12, 15, 10, 5,
                                LeadPriority.HIGH, List.of("Preferred industry: Technology", "Revenue is above $5M"));

                given(leadService.getLeadScore(1L)).willReturn(scoreResult);

                mockMvc.perform(get("/api/leads/1/score"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.leadId").value(1))
                                .andExpect(jsonPath("$.companyName").value("Nova Technology"))
                                .andExpect(jsonPath("$.totalScore").value(87))
                                .andExpect(jsonPath("$.priority").value("HIGH"))
                                .andExpect(jsonPath("$.breakdown.icpFit").value(30))
                                .andExpect(jsonPath("$.reasons[0]").value("Preferred industry: Technology"));
        }

        @Test
        void shouldRecalculateAllScores() throws Exception {
                given(leadService.recalculateAllScores()).willReturn(5);

                mockMvc.perform(post("/api/leads/recalculate-scores"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.recalculatedCount").value(5))
                                .andExpect(jsonPath("$.status").value("SUCCESS"));
        }

        @Test
        void shouldGenerateOutreachBrief() throws Exception {
                com.caprae.leadintelligence.dto.OutreachBriefResponse brief = new com.caprae.leadintelligence.dto.OutreachBriefResponse(
                                1L, "Nova Technology", "Executive summary here", List.of("Point A", "Point B"),
                                "Email", "Growth signal angle", "Hi VP, congrats on hiring", "No revenue specified");

                given(aiOutreachService.generateOutreachBrief(1L)).willReturn(brief);

                mockMvc.perform(post("/api/leads/1/outreach"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.leadId").value(1))
                                .andExpect(jsonPath("$.companyName").value("Nova Technology"))
                                .andExpect(jsonPath("$.summary").value("Executive summary here"))
                                .andExpect(jsonPath("$.recommendedChannel").value("Email"))
                                .andExpect(jsonPath("$.talkingPoints[0]").value("Point A"));
        }
}
