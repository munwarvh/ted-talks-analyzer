package com.iodigital.tedtalks.infrastructure.csv;

/**
 * Represents a validation error for a specific CSV row and field.
 */
public record ValidationError(
        long rowNumber,
        String field,
        String value,
        String errorMessage,
        ErrorType type
) {
    public enum ErrorType {
        GARBAGE_DATA,       // Non-numeric data in numeric fields
        NEGATIVE_VALUE,     // Negative numbers where positive expected
        OVERFLOW,           // Numbers too large for Long
        MISSING_FIELD,      // Required field is null/empty
        INVALID_FORMAT,     // Invalid format (e.g., date, URL)
        CONSTRAINT_VIOLATION // Other validation constraint violations
    }

    public static ValidationError garbageData(long rowNumber, String field, String value, String message) {
        return new ValidationError(rowNumber, field, value, message, ErrorType.GARBAGE_DATA);
    }

    public static ValidationError negativeValue(long rowNumber, String field, String value) {
        return new ValidationError(rowNumber, field, value,
            "Negative values are not allowed", ErrorType.NEGATIVE_VALUE);
    }

    public static ValidationError overflow(long rowNumber, String field, String value) {
        return new ValidationError(rowNumber, field, value,
            "Number is too large (exceeds Long.MAX_VALUE)", ErrorType.OVERFLOW);
    }

    public static ValidationError missingField(long rowNumber, String field) {
        return new ValidationError(rowNumber, field, null,
            "Required field is missing or empty", ErrorType.MISSING_FIELD);
    }

    public static ValidationError invalidFormat(long rowNumber, String field, String value, String message) {
        return new ValidationError(rowNumber, field, value, message, ErrorType.INVALID_FORMAT);
    }

    @Override
    public String toString() {
        return String.format("Row %d, Field '%s': %s (value='%s', type=%s)",
                rowNumber, field, errorMessage, value, type);
    }
}

