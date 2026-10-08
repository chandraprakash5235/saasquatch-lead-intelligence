package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.dto.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthResponse getHealthStatus() {
        return new HealthResponse("UP", "saasquatch-lead-intelligence");
    }
}
