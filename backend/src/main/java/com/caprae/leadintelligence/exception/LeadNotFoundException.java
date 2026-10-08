package com.caprae.leadintelligence.exception;

public class LeadNotFoundException extends RuntimeException {
    public LeadNotFoundException(Long id) {
        super("Lead not found with id: " + id);
    }
}
