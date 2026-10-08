package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.dto.LeadImportResponse;
import com.caprae.leadintelligence.dto.LeadResponse;
import com.caprae.leadintelligence.entity.Lead;
import com.caprae.leadintelligence.exception.InvalidCsvException;
import com.caprae.leadintelligence.exception.LeadNotFoundException;
import com.caprae.leadintelligence.repository.LeadRepository;
import com.caprae.leadintelligence.scoring.LeadPriority;
import com.caprae.leadintelligence.scoring.LeadScoreResult;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadScoringService leadScoringService;

    public LeadService(LeadRepository leadRepository, LeadScoringService leadScoringService) {
        this.leadRepository = leadRepository;
        this.leadScoringService = leadScoringService;
    }

    /**
     * Calculate data completeness percentage based on 10 specified fields.
     */
    public int calculateDataCompleteness(Lead lead) {
        int populatedCount = 0;
        int totalFields = 10;

        if (isNotBlank(lead.getCompanyName())) populatedCount++;
        if (isNotBlank(lead.getWebsite())) populatedCount++;
        if (isNotBlank(lead.getIndustry())) populatedCount++;
        if (lead.getRevenue() != null) populatedCount++;
        if (lead.getEmployeeCount() != null) populatedCount++;
        if (isNotBlank(lead.getLocation())) populatedCount++;
        if (isNotBlank(lead.getGrowthSignal())) populatedCount++;
        if (isNotBlank(lead.getDecisionMakerName())) populatedCount++;
        if (isNotBlank(lead.getDecisionMakerTitle())) populatedCount++;
        if (isNotBlank(lead.getDecisionMakerLinkedIn())) populatedCount++;

        return (populatedCount * 100) / totalFields;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Get paginated leads with filtering for industry, location, growthSignal, minScore, and priority.
     */
    @Transactional(readOnly = true)
    public Page<LeadResponse> getLeads(String industry, String location, String growthSignal,
                                      Integer minScore, LeadPriority priority, Pageable pageable) {
        Specification<Lead> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (isNotBlank(industry)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("industry")),
                        industry.trim().toLowerCase()
                ));
            }

            if (isNotBlank(location)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("location")),
                        location.trim().toLowerCase()
                ));
            }

            if (isNotBlank(growthSignal)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("growthSignal")),
                        growthSignal.trim().toLowerCase()
                ));
            }

            if (minScore != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("score"), minScore));
            }

            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return leadRepository.findAll(spec, pageable).map(LeadResponse::fromEntity);
    }

    /**
     * Get single lead entity by ID.
     */
    @Transactional(readOnly = true)
    public Lead getLeadEntityById(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new LeadNotFoundException(id));
    }

    /**
     * Get single lead by ID.
     */
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(Long id) {
        Lead lead = getLeadEntityById(id);
        return LeadResponse.fromEntity(lead);
    }

    /**
     * Get detailed score result and explainable breakdown for lead by ID.
     */
    @Transactional(readOnly = true)
    public LeadScoreResult getLeadScore(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new LeadNotFoundException(id));
        return leadScoringService.calculateScore(lead);
    }

    /**
     * Save a new lead manually with completeness and score calculated.
     */
    @Transactional
    public LeadResponse saveLead(Lead lead) {
        if (leadRepository.existsByCompanyNameIgnoreCase(lead.getCompanyName())) {
            throw new IllegalArgumentException("Company name already exists: " + lead.getCompanyName());
        }

        lead.setDataCompleteness(calculateDataCompleteness(lead));
        LeadScoreResult scoreResult = leadScoringService.calculateScore(lead);
        lead.setScore(scoreResult.totalScore());
        lead.setPriority(scoreResult.priority());

        Lead saved = leadRepository.save(lead);
        return LeadResponse.fromEntity(saved);
    }

    /**
     * Recalculate scores and priorities for all existing leads in DB.
     */
    @Transactional
    public int recalculateAllScores() {
        List<Lead> leads = leadRepository.findAll();
        for (Lead lead : leads) {
            lead.setDataCompleteness(calculateDataCompleteness(lead));
            LeadScoreResult scoreResult = leadScoringService.calculateScore(lead);
            lead.setScore(scoreResult.totalScore());
            lead.setPriority(scoreResult.priority());
        }
        leadRepository.saveAll(leads);
        return leads.size();
    }

    /**
     * Parse and import CSV file containing lead dataset.
     */
    @Transactional
    public LeadImportResponse importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCsvException("CSV file cannot be empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new InvalidCsvException("Invalid file extension. Must be a .csv file");
        }

        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int successfulRows = 0;
        int failedRows = 0;
        Set<String> batchCompanyNames = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            CSVParser csvParser = new CSVParser(reader, csvFormat);
            List<CSVRecord> records = csvParser.getRecords();
            totalRows = records.size();

            int rowIndex = 1;
            for (CSVRecord record : records) {
                rowIndex++;
                String companyName = getRecordValue(record, "company_name", "companyName");

                if (!isNotBlank(companyName)) {
                    errors.add("Row " + rowIndex + ": missing required field 'company_name'");
                    failedRows++;
                    continue;
                }

                String normalizedName = companyName.trim();
                String lowerName = normalizedName.toLowerCase();

                if (batchCompanyNames.contains(lowerName) || leadRepository.existsByCompanyNameIgnoreCase(normalizedName)) {
                    errors.add("Row " + rowIndex + ": duplicate company " + normalizedName);
                    failedRows++;
                    continue;
                }

                String industry = getRecordValue(record, "industry");
                String revenueStr = getRecordValue(record, "revenue");
                String employeesStr = getRecordValue(record, "employees", "employee_count", "employeeCount");
                String location = getRecordValue(record, "location");
                String growthSignal = getRecordValue(record, "growth_signal", "growthSignal");
                String decisionMaker = getRecordValue(record, "decision_maker", "decision_maker_name", "decisionMakerName");
                String website = getRecordValue(record, "website");
                String decisionMakerTitle = getRecordValue(record, "decision_maker_title", "decisionMakerTitle");
                String decisionMakerLinkedIn = getRecordValue(record, "decision_maker_linkedin", "decisionMakerLinkedIn");

                BigDecimal revenue = null;
                if (isNotBlank(revenueStr)) {
                    try {
                        String cleanRev = revenueStr.replaceAll("[$,]", "").trim();
                        revenue = new BigDecimal(cleanRev);
                    } catch (Exception e) {
                        errors.add("Row " + rowIndex + ": invalid revenue format '" + revenueStr + "'");
                        failedRows++;
                        continue;
                    }
                }

                Integer employeeCount = null;
                if (isNotBlank(employeesStr)) {
                    try {
                        String cleanEmp = employeesStr.replaceAll("[,]", "").trim();
                        employeeCount = Integer.parseInt(cleanEmp);
                    } catch (Exception e) {
                        errors.add("Row " + rowIndex + ": invalid employee count format '" + employeesStr + "'");
                        failedRows++;
                        continue;
                    }
                }

                Lead lead = new Lead();
                lead.setCompanyName(normalizedName);
                lead.setIndustry(industry);
                lead.setRevenue(revenue);
                lead.setEmployeeCount(employeeCount);
                lead.setLocation(location);
                lead.setGrowthSignal(isNotBlank(growthSignal) ? growthSignal : "None");
                lead.setDecisionMakerName(decisionMaker);
                lead.setWebsite(website);
                lead.setDecisionMakerTitle(decisionMakerTitle);
                lead.setDecisionMakerLinkedIn(decisionMakerLinkedIn);

                // Calculate completeness & scoring
                lead.setDataCompleteness(calculateDataCompleteness(lead));
                LeadScoreResult scoreResult = leadScoringService.calculateScore(lead);
                lead.setScore(scoreResult.totalScore());
                lead.setPriority(scoreResult.priority());

                leadRepository.save(lead);
                batchCompanyNames.add(lowerName);
                successfulRows++;
            }
        } catch (InvalidCsvException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidCsvException("Failed to parse CSV file: " + e.getMessage());
        }

        return new LeadImportResponse(totalRows, successfulRows, failedRows, errors);
    }

    private String getRecordValue(CSVRecord record, String... headers) {
        for (String header : headers) {
            if (record.isMapped(header)) {
                String val = record.get(header);
                if (val != null) {
                    return val.trim();
                }
            }
        }
        return null;
    }
}
