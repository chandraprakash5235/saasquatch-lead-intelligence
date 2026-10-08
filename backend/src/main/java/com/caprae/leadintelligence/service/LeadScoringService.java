package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.entity.Lead;
import com.caprae.leadintelligence.scoring.LeadPriority;
import com.caprae.leadintelligence.scoring.LeadScoreResult;
import com.caprae.leadintelligence.scoring.ScoringConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeadScoringService {

    /**
     * Calculate explainable 0-100 score for a given lead entity.
     */
    public LeadScoreResult calculateScore(Lead lead) {
        List<String> reasons = new ArrayList<>();

        int icpScore = calculateIcpScore(lead, reasons);
        int revenueScore = calculateRevenueScore(lead, reasons);
        int employeeScore = calculateEmployeeScore(lead, reasons);
        int growthScore = calculateGrowthSignalScore(lead, reasons);
        int dmScore = calculateDecisionMakerScore(lead, reasons);
        int completenessScore = calculateCompletenessScore(lead, reasons);

        int rawTotal = icpScore + revenueScore + employeeScore + growthScore + dmScore + completenessScore;
        int totalScore = Math.max(0, Math.min(100, rawTotal));

        LeadPriority priority = LeadPriority.fromScore(totalScore);

        return new LeadScoreResult(
                lead.getId(),
                lead.getCompanyName(),
                totalScore,
                icpScore,
                revenueScore,
                employeeScore,
                growthScore,
                dmScore,
                completenessScore,
                priority,
                reasons
        );
    }

    private int calculateIcpScore(Lead lead, List<String> reasons) {
        String industry = lead.getIndustry();
        if (industry == null || industry.trim().isEmpty()) {
            return 0;
        }

        String normalized = industry.trim().toLowerCase();
        if (ScoringConfig.PREFERRED_INDUSTRIES.contains(normalized)) {
            reasons.add("Preferred industry: " + lead.getIndustry());
            return 30;
        } else {
            reasons.add("Known industry: " + lead.getIndustry());
            return 15;
        }
    }

    private int calculateRevenueScore(Lead lead, List<String> reasons) {
        BigDecimal revenue = lead.getRevenue();
        if (revenue == null) {
            return 0;
        }

        if (revenue.compareTo(ScoringConfig.REV_10M) >= 0) {
            reasons.add("Revenue is $10M+");
            return 20;
        } else if (revenue.compareTo(ScoringConfig.REV_5M) >= 0) {
            reasons.add("Revenue is above $5M");
            return 15;
        } else if (revenue.compareTo(ScoringConfig.REV_2M) >= 0) {
            reasons.add("Revenue is above $2M");
            return 10;
        } else if (revenue.compareTo(ScoringConfig.REV_500K) >= 0) {
            reasons.add("Revenue is above $500K");
            return 5;
        }

        return 0;
    }

    private int calculateEmployeeScore(Lead lead, List<String> reasons) {
        Integer emp = lead.getEmployeeCount();
        if (emp == null) {
            return 0;
        }

        if (emp >= ScoringConfig.EMP_100) {
            reasons.add("100+ employees");
            return 15;
        } else if (emp >= ScoringConfig.EMP_50) {
            reasons.add("50+ employees");
            return 12;
        } else if (emp >= ScoringConfig.EMP_20) {
            reasons.add("20+ employees");
            return 8;
        } else if (emp >= ScoringConfig.EMP_10) {
            reasons.add("10+ employees");
            return 4;
        }

        return 0;
    }

    private int calculateGrowthSignalScore(Lead lead, List<String> reasons) {
        String signal = lead.getGrowthSignal();
        if (signal == null || signal.trim().isEmpty() || "None".equalsIgnoreCase(signal.trim())) {
            return 0;
        }

        String norm = signal.trim();
        if ("Hiring".equalsIgnoreCase(norm) || "Expansion".equalsIgnoreCase(norm) || "New Location".equalsIgnoreCase(norm)) {
            reasons.add("Strong growth signal: " + norm);
            return 15;
        } else if ("Funding".equalsIgnoreCase(norm) || "Acquisition".equalsIgnoreCase(norm)) {
            reasons.add("Significant growth signal: " + norm);
            return 12;
        } else if ("Partnership".equalsIgnoreCase(norm)) {
            reasons.add("Growth signal: " + norm);
            return 8;
        }

        return 0;
    }

    private int calculateDecisionMakerScore(Lead lead, List<String> reasons) {
        boolean hasName = isNotBlank(lead.getDecisionMakerName());
        boolean hasTitle = isNotBlank(lead.getDecisionMakerTitle());
        boolean hasLinkedIn = isNotBlank(lead.getDecisionMakerLinkedIn());

        if (hasName && hasTitle && hasLinkedIn) {
            reasons.add("Decision maker information is complete");
            return 10;
        } else if (hasName && hasTitle) {
            reasons.add("Decision maker identified (Name & Title)");
            return 8;
        } else if (hasName) {
            reasons.add("Decision maker name identified");
            return 5;
        }

        return 0;
    }

    private int calculateCompletenessScore(Lead lead, List<String> reasons) {
        Integer comp = lead.getDataCompleteness();
        if (comp == null) {
            return 0;
        }

        if (comp >= 90) {
            reasons.add("High data completeness");
            return 10;
        } else if (comp >= 75) {
            reasons.add("Good data completeness");
            return 8;
        } else if (comp >= 50) {
            reasons.add("Moderate data completeness");
            return 5;
        } else if (comp >= 25) {
            reasons.add("Basic data completeness");
            return 2;
        }

        return 0;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
