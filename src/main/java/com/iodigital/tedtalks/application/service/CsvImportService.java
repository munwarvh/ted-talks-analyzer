package com.iodigital.tedtalks.application.service;

import com.iodigital.tedtalks.application.port.CsvImporter;
import com.iodigital.tedtalks.domain.service.ImportResult;
import com.iodigital.tedtalks.domain.service.ImportStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class CsvImportService implements CsvImporter {

    private final CsvImportTransactionService transactionService;
    private final ConcurrentMap<String, ImportResult> importResults = new ConcurrentHashMap<>();

    public CsvImportService(CsvImportTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    @Transactional
    public ImportResult importFromCsv(String importId, InputStream csvStream) {
        ImportResult initialResult = ImportResult.create(importId);
        importResults.put(importId, initialResult);

        try {
            log.info("Starting synchronous CSV import: {}", importId);

            // Update status to processing
            ImportResult processingResult = new ImportResult(
                    importId,
                    initialResult.startedAt(),
                    null,
                    ImportResult.Status.PROCESSING,
                    new ImportStatistics(),
                    List.of()
            );
            importResults.put(importId, processingResult);

            ImportStatistics stats = transactionService.processCsvStreamWithTransaction(csvStream, importId);

            // Create final result
            ImportResult finalResult = new ImportResult(
                    importId,
                    initialResult.startedAt(),
                    Instant.now(),
                    determineStatus(stats),
                    stats,
                    List.of()
            );

            importResults.put(importId, finalResult);
            log.info("Import completed: {}", importId);

            return finalResult;

        } catch (Exception e) {
            log.error("Import failed: {}", importId, e);

            ImportResult failedResult = new ImportResult(
                    importId,
                    initialResult.startedAt(),
                    Instant.now(),
                    ImportResult.Status.FAILED,
                    new ImportStatistics(),
                    List.of(e.getMessage())
            );

            importResults.put(importId, failedResult);
            return failedResult;
        }
    }


    // Async version for background processing with proper transaction management
    @Async("csvImportExecutor")
    public void importFromCsvAsync(String importId, InputStream csvStream) {
        log.info("Starting async CSV import: {}", importId);

        ImportResult initialResult = ImportResult.create(importId);
        importResults.put(importId, initialResult);

        try {
            // Update status to processing
            ImportResult processingResult = new ImportResult(
                    importId,
                    initialResult.startedAt(),
                    null,
                    ImportResult.Status.PROCESSING,
                    new ImportStatistics(),
                    List.of()
            );
            importResults.put(importId, processingResult);

            // Process the CSV with proper transaction via separate service
            // This ensures Spring's transaction proxy works correctly
            ImportStatistics stats = transactionService.processCsvStreamWithTransaction(csvStream, importId);

            // Create final result
            ImportResult finalResult = new ImportResult(
                    importId,
                    initialResult.startedAt(),
                    Instant.now(),
                    determineStatus(stats),
                    stats,
                    List.of()
            );

            importResults.put(importId, finalResult);
            log.info("Async import completed: {}", importId);

        } catch (Exception e) {
            log.error("Async import failed: {}", importId, e);

            ImportResult failedResult = new ImportResult(
                    importId,
                    initialResult.startedAt(),
                    Instant.now(),
                    ImportResult.Status.FAILED,
                    new ImportStatistics(),
                    List.of(e.getMessage())
            );

            importResults.put(importId, failedResult);
        }
    }

    private ImportResult.Status determineStatus(ImportStatistics stats) {
        if (stats.getTotal() == 0) {
            return ImportResult.Status.COMPLETED;
        } else if (stats.getFailed() > 0) {
            return ImportResult.Status.PARTIALLY_COMPLETED;
        } else {
            return ImportResult.Status.COMPLETED;
        }
    }


    public Optional<ImportResult> getImportResult(String importId) {
        return Optional.ofNullable(importResults.get(importId));
    }
}