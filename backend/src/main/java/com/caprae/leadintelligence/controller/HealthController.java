package com.caprae.leadintelligence.controller;

import com.caprae.leadintelligence.dto.HealthResponse;
import com.caprae.leadintelligence.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final HealthService healthService;

    // Constructor injection
    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealthStatus() {
        HealthResponse response = healthService.getHealthStatus();
        return ResponseEntity.ok(response);
    }
}
