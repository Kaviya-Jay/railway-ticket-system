package com.railwayticketsystem.railwayticketsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user exceeds the 3-ticket limit in rolling 24 hours (NIC-based quota)
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {
        super(message);
    }

    public QuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
