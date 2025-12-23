package com.iodigital.tedtalks.presentation.rest.response;

import com.iodigital.tedtalks.infrastructure.csv.ValidationError;

import java.util.List;

/**
 * Response DTO for validation error report
 */
public record ValidationErrorReport(
        String importId,
        int totalErrors,
        long failedRows,
        List<ValidationError> errors
) {
}

