package com.iodigital.tedtalks.presentation.rest.response;

import java.time.Instant;

/**
 * Response DTO for CSV import initiation
 */
public record ImportResponse(
        String importId,
        String message,
        Instant startedAt
) {
}

