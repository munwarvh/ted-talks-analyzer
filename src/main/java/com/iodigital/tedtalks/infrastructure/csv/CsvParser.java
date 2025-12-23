package com.iodigital.tedtalks.infrastructure.csv;

import com.iodigital.tedtalks.infrastructure.csv.TedTalkCsvMapper.CsvImportRecord;
import com.iodigital.tedtalks.infrastructure.csv.TedTalkCsvMapper.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class CsvParser {

    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT
            .builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .setIgnoreEmptyLines(true)
            .setIgnoreSurroundingSpaces(true)
            .build();

    private final TedTalkCsvMapper mapper;

    public CsvParser(TedTalkCsvMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * New safe parsing method that returns ValidationResult containing either
     * the successfully parsed record or validation errors.
     * This allows the caller to collect and report all errors.
     */
    public Stream<ValidationResult> parseSafe(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            org.apache.commons.csv.CSVParser csvParser = new org.apache.commons.csv.CSVParser(reader, CSV_FORMAT);

            return StreamSupport.stream(csvParser.spliterator(), false)
                    .map(record -> {
                        ValidationResult result = mapper.mapCsvRecordSafe(record);
                        if (result.hasErrors()) {
                            log.debug("Row {} validation failed with {} errors",
                                    record.getRecordNumber(), result.errors().size());
                        }
                        return result;
                    })
                    .onClose(() -> {
                        try {
                            csvParser.close();
                        } catch (IOException e) {
                            log.error("Failed to close CSV parser", e);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file", e);
        }
    }

    /**
     * Legacy parsing method - returns Optional<CsvImportRecord>.
     * @deprecated Use parseSafe() instead for better error handling.
     */
    @Deprecated
    public Stream<Optional<CsvImportRecord>> parse(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            org.apache.commons.csv.CSVParser csvParser = new org.apache.commons.csv.CSVParser(reader, CSV_FORMAT);

            return StreamSupport.stream(csvParser.spliterator(), false)
                    .map(record -> {
                        try {
                            return Optional.of(mapper.mapCsvRecord(record));
                        } catch (CsvValidationException e) {
                            log.warn("Failed to parse CSV record at line {}: {}",
                                    record.getRecordNumber(), e.getMessage());
                            return Optional.<CsvImportRecord>empty();
                        }
                    })
                    .onClose(() -> {
                        try {
                            csvParser.close();
                        } catch (IOException e) {
                            log.error("Failed to close CSV parser", e);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file", e);
        }
    }
}
