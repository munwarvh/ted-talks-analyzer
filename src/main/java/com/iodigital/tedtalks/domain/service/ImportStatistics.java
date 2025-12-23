package com.iodigital.tedtalks.domain.service;

import com.iodigital.tedtalks.infrastructure.csv.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ImportStatistics {
    private final AtomicLong totalRecords = new AtomicLong();
    private final AtomicLong successfulRecords = new AtomicLong();
    private final AtomicLong failedRecords = new AtomicLong();
    private final AtomicLong skippedRecords = new AtomicLong();
    private final ConcurrentHashMap<Long, List<ValidationError>> validationErrors = new ConcurrentHashMap<>();

    public void incrementTotal() { totalRecords.incrementAndGet(); }
    public void incrementSuccessful() { successfulRecords.incrementAndGet(); }
    public void incrementFailed() { failedRecords.incrementAndGet(); }
    public void incrementSkipped() { skippedRecords.incrementAndGet(); }

    public void addValidationErrors(long rowNumber, List<ValidationError> errors) {
        if (errors != null && !errors.isEmpty()) {
            validationErrors.put(rowNumber, new ArrayList<>(errors));
        }
    }

    // Getters and utility methods
    public long getTotal() { return totalRecords.get(); }
    public long getSuccessful() { return successfulRecords.get(); }
    public long getFailed() { return failedRecords.get(); }
    public long getSkipped() { return skippedRecords.get(); }

    public List<ValidationError> getAllValidationErrors() {
        List<ValidationError> allErrors = new ArrayList<>();
        validationErrors.values().forEach(allErrors::addAll);
        return allErrors;
    }

    public int getValidationErrorCount() {
        return validationErrors.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    public double getSuccessRate() {
        return totalRecords.get() > 0 ?
                (double) successfulRecords.get() / totalRecords.get() * 100 : 0;
    }
}

