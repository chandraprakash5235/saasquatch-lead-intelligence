package com.caprae.leadintelligence.controller;

import com.caprae.leadintelligence.dto.LeadImportResponse;
import com.caprae.leadintelligence.dto.LeadResponse;
import com.caprae.leadintelligence.dto.PagedResponse;
import com.caprae.leadintelligence.scoring.LeadPriority;
import com.caprae.leadintelligence.scoring.LeadScoreResult;
import com.caprae.leadintelligence.dto.OutreachBriefResponse;
import com.caprae.leadintelligence.service.AIOutreachService;
import com.caprae.leadintelligence.service.LeadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;
    private final AIOutreachService aiOutreachService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "companyName", "industry", "revenue", "employeeCount",
            "dataCompleteness", "createdAt", "location", "growthSignal",
            "score", "priority"
    );

    public LeadController(LeadService leadService, AIOutreachService aiOutreachService) {
        this.leadService = leadService;
        this.aiOutreachService = aiOutreachService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<LeadResponse>> getLeads(
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String growthSignal,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) LeadPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "score") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "score";
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, safeSortBy));
        Page<LeadResponse> leadsPage = leadService.getLeads(industry, location, growthSignal, minScore, priority, pageable);

        return ResponseEntity.ok(PagedResponse.fromPage(leadsPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(@PathVariable Long id) {
        LeadResponse lead = leadService.getLeadById(id);
        return ResponseEntity.ok(lead);
    }

    @GetMapping("/{id}/score")
    public ResponseEntity<LeadScoreResult> getLeadScore(@PathVariable Long id) {
        LeadScoreResult scoreResult = leadService.getLeadScore(id);
        return ResponseEntity.ok(scoreResult);
    }

    @PostMapping("/{id}/outreach")
    public ResponseEntity<OutreachBriefResponse> generateOutreachBrief(@PathVariable Long id) {
        OutreachBriefResponse brief = aiOutreachService.generateOutreachBrief(id);
        return ResponseEntity.ok(brief);
    }

    @PostMapping("/recalculate-scores")
    public ResponseEntity<Map<String, Object>> recalculateScores() {
        int count = leadService.recalculateAllScores();
        return ResponseEntity.ok(Map.of(
                "recalculatedCount", count,
                "status", "SUCCESS",
                "message", "Successfully recalculated scores for " + count + " lead(s)"
        ));
    }

    @PostMapping("/import")
    public ResponseEntity<LeadImportResponse> importLeads(@RequestParam("file") MultipartFile file) {
        LeadImportResponse response = leadService.importCsv(file);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
