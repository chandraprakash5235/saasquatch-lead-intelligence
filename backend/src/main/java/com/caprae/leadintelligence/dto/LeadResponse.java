package com.caprae.leadintelligence.dto;

import com.caprae.leadintelligence.entity.Lead;
import com.caprae.leadintelligence.scoring.LeadPriority;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LeadResponse(
        Long id,
        String companyName,
        String website,
        String industry,
        BigDecimal revenue,
        Integer employeeCount,
        String location,
        String growthSignal,
        String decisionMakerName,
        String decisionMakerTitle,
        String decisionMakerLinkedIn,
        Integer dataCompleteness,
        Integer score,
        LeadPriority priority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LeadResponse fromEntity(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getCompanyName(),
                lead.getWebsite(),
                lead.getIndustry(),
                lead.getRevenue(),
                lead.getEmployeeCount(),
                lead.getLocation(),
                lead.getGrowthSignal(),
                lead.getDecisionMakerName(),
                lead.getDecisionMakerTitle(),
                lead.getDecisionMakerLinkedIn(),
                lead.getDataCompleteness(),
                lead.getScore(),
                lead.getPriority(),
                lead.getCreatedAt(),
                lead.getUpdatedAt()
        );
    }
}
