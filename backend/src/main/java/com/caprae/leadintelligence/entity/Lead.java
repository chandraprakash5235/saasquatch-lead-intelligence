package com.caprae.leadintelligence.entity;

import com.caprae.leadintelligence.scoring.LeadPriority;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "website")
    private String website;

    @Column(name = "industry")
    private String industry;

    @Column(name = "revenue", precision = 19, scale = 2)
    private BigDecimal revenue;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "location")
    private String location;

    @Column(name = "growth_signal")
    private String growthSignal;

    @Column(name = "decision_maker_name")
    private String decisionMakerName;

    @Column(name = "decision_maker_title")
    private String decisionMakerTitle;

    @Column(name = "decision_maker_linkedin")
    private String decisionMakerLinkedIn;

    @Column(name = "data_completeness")
    private Integer dataCompleteness;

    @Column(name = "score")
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private LeadPriority priority;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Lead() {
    }

    public Lead(String companyName, String website, String industry, BigDecimal revenue,
                Integer employeeCount, String location, String growthSignal,
                String decisionMakerName, String decisionMakerTitle, String decisionMakerLinkedIn) {
        this.companyName = companyName;
        this.website = website;
        this.industry = industry;
        this.revenue = revenue;
        this.employeeCount = employeeCount;
        this.location = location;
        this.growthSignal = growthSignal;
        this.decisionMakerName = decisionMakerName;
        this.decisionMakerTitle = decisionMakerTitle;
        this.decisionMakerLinkedIn = decisionMakerLinkedIn;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGrowthSignal() {
        return growthSignal;
    }

    public void setGrowthSignal(String growthSignal) {
        this.growthSignal = growthSignal;
    }

    public String getDecisionMakerName() {
        return decisionMakerName;
    }

    public void setDecisionMakerName(String decisionMakerName) {
        this.decisionMakerName = decisionMakerName;
    }

    public String getDecisionMakerTitle() {
        return decisionMakerTitle;
    }

    public void setDecisionMakerTitle(String decisionMakerTitle) {
        this.decisionMakerTitle = decisionMakerTitle;
    }

    public String getDecisionMakerLinkedIn() {
        return decisionMakerLinkedIn;
    }

    public void setDecisionMakerLinkedIn(String decisionMakerLinkedIn) {
        this.decisionMakerLinkedIn = decisionMakerLinkedIn;
    }

    public Integer getDataCompleteness() {
        return dataCompleteness;
    }

    public void setDataCompleteness(Integer dataCompleteness) {
        this.dataCompleteness = dataCompleteness;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LeadPriority getPriority() {
        return priority;
    }

    public void setPriority(LeadPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
