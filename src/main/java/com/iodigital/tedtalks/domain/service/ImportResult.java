package com.iodigital.tedtalks.domain.service;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record ImportResult(
        String importId,
        Instant startedAt,
        Instant completedAt,
        Status status,
        ImportStatistics statistics,
        List<String> errors
) {
    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED, PARTIALLY_COMPLETED
    }

    public static ImportResult create(String importId) {
        return new ImportResult(
                importId,
                Instant.now(),
                null,
                Status.PENDING,
                new ImportStatistics(),
                new ArrayList<>()
        );
    }

    /**
     * Calculate the duration of the import in seconds.
     * Returns null if import is not yet completed.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Double getDurationSeconds() {
        if (startedAt != null && completedAt != null) {
            return Duration.between(startedAt, completedAt).toMillis() / 1000.0;
        }
        return null;
    }
}

