package com.caprae.leadintelligence.exception;

public class AIOutreachException extends RuntimeException {
    public AIOutreachException(String message) {
        super(message);
    }

    public AIOutreachException(String message, Throwable cause) {
        super(message, cause);
    }
}
