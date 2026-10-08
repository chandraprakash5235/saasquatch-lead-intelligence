package com.caprae.leadintelligence.scoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LeadScoreResult(
        Long leadId,
        String companyName,
        int totalScore,
        int icpFitScore,
        int revenueFitScore,
        int employeeFitScore,
        int growthSignalScore,
        int decisionMakerScore,
        int dataCompletenessScore,
        LeadPriority priority,
        List<String> reasons
) {
    @JsonProperty("breakdown")
    public ScoreBreakdown breakdown() {
        return new ScoreBreakdown(
                icpFitScore,
                revenueFitScore,
                employeeFitScore,
                growthSignalScore,
                decisionMakerScore,
                dataCompletenessScore
        );
    }

    public record ScoreBreakdown(
            int icpFit,
            int revenueFit,
            int employeeFit,
            int growthSignal,
            int decisionMaker,
            int dataCompleteness
    ) {}
}
