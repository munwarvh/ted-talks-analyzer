package com.iodigital.tedtalks.presentation.rest;

import com.iodigital.tedtalks.presentation.rest.request.ImportRequest;
import com.iodigital.tedtalks.presentation.rest.response.ImportResponse;
import com.iodigital.tedtalks.presentation.rest.response.ValidationErrorReport;
import com.iodigital.tedtalks.application.port.CsvImporter;
import com.iodigital.tedtalks.application.service.CsvImportService;
import com.iodigital.tedtalks.domain.service.ImportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/import")
@Tag(name = "Import", description = "CSV Import Operations")
@Slf4j
@Validated
public class ImportController {

    private final CsvImportService importService;

    public ImportController(CsvImportService importService) {
        this.importService = importService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import TedTalks from CSV")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<ImportResponse> importCsv(
            @Valid @ModelAttribute ImportRequest request) {

        log.info("Received CSV import request: {} ({} bytes)",
                request.file().getOriginalFilename(),
                request.file().getSize());

        String importId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();

        try {
            // Read the file bytes in the request thread before it gets deleted
            byte[] fileBytes = request.file().getBytes();
            String filename = request.file().getOriginalFilename();

            log.info("File read successfully: {} ({} bytes)", filename, fileBytes.length);

            // Start async import using Spring's @Async with proper transaction management
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(fileBytes);
            importService.importFromCsvAsync(importId, inputStream);

            return ResponseEntity.accepted()
                    .body(new ImportResponse(
                            importId,
                            "Import started",
                            startedAt
                    ));

        } catch (Exception e) {
            log.error("Import request failed", e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Failed to start import: " + e.getMessage()
            );
        }
    }

    @GetMapping("/{importId}/status")
    @Operation(summary = "Get import status")
    public ResponseEntity<ImportResult> getImportStatus(
            @PathVariable String importId) {

        return importService.getImportResult(importId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Import not found: " + importId
                ));
    }

    @GetMapping("/{importId}/errors")
    @Operation(summary = "Get validation errors for import")
    public ResponseEntity<ValidationErrorReport> getValidationErrors(
            @PathVariable String importId) {

        return importService.getImportResult(importId)
                .map(result -> {
                    var errors = result.statistics().getAllValidationErrors();
                    var report = new ValidationErrorReport(
                            importId,
                            result.statistics().getValidationErrorCount(),
                            result.statistics().getFailed(),
                            errors
                    );
                    return ResponseEntity.ok(report);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Import not found: " + importId
                ));
    }
}

