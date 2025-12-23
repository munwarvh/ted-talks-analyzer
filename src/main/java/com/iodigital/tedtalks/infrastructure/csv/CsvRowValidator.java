package com.iodigital.tedtalks.infrastructure.csv;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Enterprise-grade CSV row validator that handles dirty data gracefully.
 * Validates all fields and collects detailed error information.
 */
@Component
@Slf4j
public class CsvRowValidator {

    private static final long MAX_LONG_VALUE = Long.MAX_VALUE;
    private static final String MAX_LONG_STR = String.valueOf(Long.MAX_VALUE);

    /**
     * Validates a CSV record and returns all validation errors.
     * Returns empty list if validation passes.
     */
    public List<ValidationError> validate(CSVRecord record) {
        List<ValidationError> errors = new ArrayList<>();
        long rowNumber = record.getRecordNumber();

        // Validate required fields
        validateRequiredField(record, "title", rowNumber, errors);
        validateRequiredField(record, "author", rowNumber, errors);
        validateRequiredField(record, "date", rowNumber, errors);
        validateRequiredField(record, "views", rowNumber, errors);
        validateRequiredField(record, "likes", rowNumber, errors);
        validateRequiredField(record, "link", rowNumber, errors);

        // If required fields missing, no point validating further
        if (!errors.isEmpty()) {
            return errors;
        }

        // Validate numeric fields
        validateNumericField(record, "views", rowNumber, errors);
        validateNumericField(record, "likes", rowNumber, errors);

        return errors;
    }

    private void validateRequiredField(CSVRecord record, String fieldName, long rowNumber, List<ValidationError> errors) {
        try {
            String value = record.get(fieldName);
            if (value == null || value.trim().isEmpty()) {
                errors.add(ValidationError.missingField(rowNumber, fieldName));
            }
        } catch (IllegalArgumentException e) {
            errors.add(ValidationError.missingField(rowNumber, fieldName));
        }
    }

    private void validateNumericField(CSVRecord record, String fieldName, long rowNumber, List<ValidationError> errors) {
        String value = record.get(fieldName);
        if (value == null || value.trim().isEmpty()) {
            return; // Already caught by required field validation
        }

        String cleanValue = cleanNumericString(value);

        // Check for garbage data (non-numeric characters remaining)
        if (!isNumeric(cleanValue)) {
            errors.add(ValidationError.garbageData(rowNumber, fieldName, value,
                    "Contains non-numeric characters: '" + value + "'"));
            return;
        }

        // Check for negative values
        if (cleanValue.startsWith("-")) {
            errors.add(ValidationError.negativeValue(rowNumber, fieldName, value));
            return;
        }

        // Check for overflow (number too large for Long)
        if (isOverflow(cleanValue)) {
            errors.add(ValidationError.overflow(rowNumber, fieldName, value));
            return;
        }
    }

    /**
     * Cleans numeric string by removing commas and whitespace.
     * Preserves minus sign and digits only.
     */
    private String cleanNumericString(String value) {
        // Remove commas, spaces, and other common separators
        String cleaned = value.trim()
                .replace(",", "")
                .replace(" ", "")
                .replace("_", "");

        return cleaned;
    }

    /**
     * Checks if string is purely numeric (with optional leading minus sign).
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        int startIndex = 0;
        if (str.charAt(0) == '-') {
            if (str.length() == 1) {
                return false;
            }
            startIndex = 1;
        }

        for (int i = startIndex; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if numeric string exceeds Long.MAX_VALUE.
     * Uses BigInteger for safe comparison.
     */
    private boolean isOverflow(String numericStr) {
        try {
            // If it parses as long without exception, it's fine
            Long.parseLong(numericStr);
            return false;
        } catch (NumberFormatException e) {
            // Could be overflow or truly invalid
            // If we got here and isNumeric passed, it's overflow
            return true;
        }
    }

    /**
     * Safe parsing of numeric field that returns Optional.
     * Returns empty if parsing fails for any reason.
     */
    public Optional<Long> parseNumericField(String value, String fieldName, long rowNumber) {
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }

        String cleanValue = cleanNumericString(value);

        if (!isNumeric(cleanValue)) {
            log.debug("Row {}, field '{}': Garbage data detected: '{}'", rowNumber, fieldName, value);
            return Optional.empty();
        }

        if (cleanValue.startsWith("-")) {
            log.debug("Row {}, field '{}': Negative value detected: '{}'", rowNumber, fieldName, value);
            return Optional.empty();
        }

        try {
            long parsed = Long.parseLong(cleanValue);
            return Optional.of(parsed);
        } catch (NumberFormatException e) {
            log.debug("Row {}, field '{}': Overflow detected: '{}'", rowNumber, fieldName, value);
            return Optional.empty();
        }
    }
}

