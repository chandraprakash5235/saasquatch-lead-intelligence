package com.caprae.leadintelligence.scoring;

public enum LeadPriority {
    HIGH,
    MEDIUM,
    LOW;

    public static LeadPriority fromScore(int score) {
        if (score >= 80) {
            return HIGH;
        } else if (score >= 60) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}
