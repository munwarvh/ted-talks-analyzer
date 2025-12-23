package com.iodigital.tedtalks.infrastructure.csv;

import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.model.valueobject.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TedTalkCsvMapper {

    private final CsvRowValidator validator;

    public TedTalkCsvMapper(CsvRowValidator validator) {
        this.validator = validator;
    }

    public record CsvImportRecord(Speaker speaker, TedTalk talk) {}

    public record ValidationResult(
            Optional<CsvImportRecord> record,
            List<ValidationError> errors
    ) {
        public boolean isValid() {
            return errors.isEmpty() && record.isPresent();
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public static ValidationResult success(CsvImportRecord record) {
            return new ValidationResult(Optional.of(record), List.of());
        }

        public static ValidationResult failure(List<ValidationError> errors) {
            return new ValidationResult(Optional.empty(), errors);
        }
    }

    /**
     * Maps a CSV record to domain objects with full validation.
     * Returns ValidationResult containing either the mapped record or validation errors.
     * This method NEVER throws exceptions - it returns errors instead.
     */
    public ValidationResult mapCsvRecordSafe(CSVRecord record) {
        long rowNumber = record.getRecordNumber();

        // Step 1: Validate the entire row
        List<ValidationError> validationErrors = validator.validate(record);

        if (!validationErrors.isEmpty()) {
            log.warn("Row {} failed validation with {} errors", rowNumber, validationErrors.size());
            for (ValidationError error : validationErrors) {
                log.debug("  - {}", error);
            }
            return ValidationResult.failure(validationErrors);
        }

        // Step 2: Extract and parse fields with additional safety
        try {
            String title = validateAndTrim(record, "title");
            String speakerName = validateAndTrim(record, "author");
            String dateStr = validateAndTrim(record, "date");
            String viewsStr = validateAndTrim(record, "views");
            String likesStr = validateAndTrim(record, "likes");
            String linkStr = validateAndTrim(record, "link");

            // Parse value objects with safe parsing
            TalkDate talkDate = parseDate(dateStr, rowNumber);
            Views views = parseViewsSafe(viewsStr, rowNumber);
            Likes likes = parseLikesSafe(likesStr, rowNumber);
            Link link = parseLink(linkStr, rowNumber);

            // Create Speaker (without ID yet - will be resolved during import)
            Speaker speaker = Speaker.create(speakerName, null);

            // Create TedTalk (without ID yet)
            TedTalk talk = TedTalk.create(title, speaker, talkDate, views, likes, link);

            return ValidationResult.success(new CsvImportRecord(speaker, talk));

        } catch (CsvValidationException e) {
            log.warn("Row {} parsing failed: {}", rowNumber, e.getMessage());
            return ValidationResult.failure(List.of(
                    ValidationError.invalidFormat(rowNumber, e.getField(), e.getValue(), e.getMessage())
            ));
        } catch (Exception e) {
            log.error("Row {} unexpected error: {}", rowNumber, e.getMessage(), e);
            return ValidationResult.failure(List.of(
                    ValidationError.invalidFormat(rowNumber, "unknown", null, "Unexpected error: " + e.getMessage())
            ));
        }
    }

    /**
     * Legacy method for backward compatibility - throws exception on validation errors.
     * @deprecated Use mapCsvRecordSafe() instead for better error handling.
     */
    @Deprecated
    public CsvImportRecord mapCsvRecord(CSVRecord record) throws CsvValidationException {
        ValidationResult result = mapCsvRecordSafe(record);
        if (result.hasErrors()) {
            ValidationError firstError = result.errors().get(0);
            throw new CsvValidationException(
                    firstError.rowNumber(),
                    firstError.field(),
                    firstError.value(),
                    firstError.errorMessage()
            );
        }
        return result.record().orElseThrow(() ->
                new CsvValidationException(record.getRecordNumber(), "record", null, "Unknown validation error")
        );
    }

    private String validateAndTrim(CSVRecord record, String column) {
        String value = record.get(column);
        if (value == null || value.trim().isEmpty()) {
            throw new CsvValidationException(
                    record.getRecordNumber(),
                    column,
                    value,
                    "Value cannot be empty"
            );
        }
        return value.trim();
    }

    private TalkDate parseDate(String dateStr, long recordNumber) {
        try {
            return TalkDate.fromString(dateStr);
        } catch (IllegalArgumentException e) {
            throw new CsvValidationException(
                    recordNumber,
                    "date",
                    dateStr,
                    e.getMessage()
            );
        }
    }

    /**
     * Safe parsing of views that handles garbage data, negative numbers, and overflow.
     */
    private Views parseViewsSafe(String viewsStr, long recordNumber) {
        Optional<Long> parsedViews = validator.parseNumericField(viewsStr, "views", recordNumber);

        if (parsedViews.isEmpty()) {
            throw new CsvValidationException(
                    recordNumber,
                    "views",
                    viewsStr,
                    "Invalid views value (must be a positive number within Long range)"
            );
        }

        try {
            return new Views(parsedViews.get());
        } catch (IllegalArgumentException e) {
            throw new CsvValidationException(
                    recordNumber,
                    "views",
                    viewsStr,
                    e.getMessage()
            );
        }
    }

    /**
     * Safe parsing of likes that handles garbage data, negative numbers, and overflow.
     */
    private Likes parseLikesSafe(String likesStr, long recordNumber) {
        Optional<Long> parsedLikes = validator.parseNumericField(likesStr, "likes", recordNumber);

        if (parsedLikes.isEmpty()) {
            throw new CsvValidationException(
                    recordNumber,
                    "likes",
                    likesStr,
                    "Invalid likes value (must be a positive number within Long range)"
            );
        }

        try {
            return new Likes(parsedLikes.get());
        } catch (IllegalArgumentException e) {
            throw new CsvValidationException(
                    recordNumber,
                    "likes",
                    likesStr,
                    e.getMessage()
            );
        }
    }

    /**
     * Legacy parsing methods - kept for backward compatibility but not recommended.
     */
    @Deprecated
    private Views parseViews(String viewsStr, long recordNumber) {
        try {
            // Remove any non-numeric characters (like commas)
            String cleanViews = viewsStr.replaceAll("[^0-9]", "");
            long views = Long.parseLong(cleanViews);
            return new Views(views);
        } catch (NumberFormatException e) {
            throw new CsvValidationException(
                    recordNumber,
                    "views",
                    viewsStr,
                    "Invalid number format for views"
            );
        }
    }

    @Deprecated
    private Likes parseLikes(String likesStr, long recordNumber) {
        try {
            String cleanLikes = likesStr.replaceAll("[^0-9]", "");
            long likes = Long.parseLong(cleanLikes);
            return new Likes(likes);
        } catch (NumberFormatException e) {
            throw new CsvValidationException(
                    recordNumber,
                    "likes",
                    likesStr,
                    "Invalid number format for likes"
            );
        }
    }

    private Link parseLink(String linkStr, long recordNumber) {
        try {
            return new Link(linkStr);
        } catch (IllegalArgumentException e) {
            throw new CsvValidationException(
                    recordNumber,
                    "link",
                    linkStr,
                    e.getMessage()
            );
        }
    }
}
