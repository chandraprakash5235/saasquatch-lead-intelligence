package com.caprae.leadintelligence.dto;

import java.util.List;

public class OutreachBriefResponse {
    private Long leadId;
    private String companyName;
    private String summary;
    private List<String> talkingPoints;
    private String recommendedChannel;
    private String personalizationAngle;
    private String suggestedEmailOpening;
    private String riskNotes;

    public OutreachBriefResponse() {
    }

    public OutreachBriefResponse(Long leadId, String companyName, String summary, List<String> talkingPoints,
                                String recommendedChannel, String personalizationAngle,
                                String suggestedEmailOpening, String riskNotes) {
        this.leadId = leadId;
        this.companyName = companyName;
        this.summary = summary;
        this.talkingPoints = talkingPoints;
        this.recommendedChannel = recommendedChannel;
        this.personalizationAngle = personalizationAngle;
        this.suggestedEmailOpening = suggestedEmailOpening;
        this.riskNotes = riskNotes;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getTalkingPoints() {
        return talkingPoints;
    }

    public void setTalkingPoints(List<String> talkingPoints) {
        this.talkingPoints = talkingPoints;
    }

    public String getRecommendedChannel() {
        return recommendedChannel;
    }

    public void setRecommendedChannel(String recommendedChannel) {
        this.recommendedChannel = recommendedChannel;
    }

    public String getPersonalizationAngle() {
        return personalizationAngle;
    }

    public void setPersonalizationAngle(String personalizationAngle) {
        this.personalizationAngle = personalizationAngle;
    }

    public String getSuggestedEmailOpening() {
        return suggestedEmailOpening;
    }

    public void setSuggestedEmailOpening(String suggestedEmailOpening) {
        this.suggestedEmailOpening = suggestedEmailOpening;
    }

    public String getRiskNotes() {
        return riskNotes;
    }

    public void setRiskNotes(String riskNotes) {
        this.riskNotes = riskNotes;
    }
}
