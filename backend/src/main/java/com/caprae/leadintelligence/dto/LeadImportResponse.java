package com.caprae.leadintelligence.dto;

import java.util.List;

public record LeadImportResponse(
        int totalRows,
        int successfulRows,
        int failedRows,
        List<String> errors
) {}
