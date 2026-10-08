package com.caprae.leadintelligence.scoring;

import java.math.BigDecimal;
import java.util.Set;

public class ScoringConfig {

    public static final Set<String> PREFERRED_INDUSTRIES = Set.of(
            "saas",
            "technology",
            "software",
            "manufacturing",
            "healthcare",
            "logistics",
            "financial services",
            "professional services"
    );

    // Revenue Fit Thresholds
    public static final BigDecimal REV_10M = new BigDecimal("10000000");
    public static final BigDecimal REV_5M  = new BigDecimal("5000000");
    public static final BigDecimal REV_2M  = new BigDecimal("2000000");
    public static final BigDecimal REV_500K = new BigDecimal("500000");

    // Employee Fit Thresholds
    public static final int EMP_100 = 100;
    public static final int EMP_50  = 50;
    public static final int EMP_20  = 20;
    public static final int EMP_10  = 10;
}
