package com.iodigital.tedtalks.application.service;

import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.repository.SpeakerRepository;
import com.iodigital.tedtalks.domain.repository.TedTalkRepository;
import com.iodigital.tedtalks.domain.service.ImportStatistics;
import com.iodigital.tedtalks.infrastructure.csv.CsvParser;
import com.iodigital.tedtalks.infrastructure.csv.TedTalkCsvMapper.CsvImportRecord;
import com.iodigital.tedtalks.infrastructure.csv.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CsvImportTransactionService {

    private final CsvParser csvParser;
    private final SpeakerRepository speakerRepository;
    private final TedTalkRepository tedTalkRepository;

    public CsvImportTransactionService(CsvParser csvParser,
                                       SpeakerRepository speakerRepository,
                                       TedTalkRepository tedTalkRepository) {
        this.csvParser = csvParser;
        this.speakerRepository = speakerRepository;
        this.tedTalkRepository = tedTalkRepository;
    }

    /**
     * Process CSV stream within a transaction.
     * This method is in a separate service to ensure Spring's transaction proxy works.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportStatistics processCsvStreamWithTransaction(InputStream csvStream, String importId) {
        log.info("Processing CSV stream with transaction: {}", importId);

        ImportStatistics stats = new ImportStatistics();
        Map<String, Speaker> speakerCache = new HashMap<>();
        List<TedTalk> talkBatch = new ArrayList<>(1000);

        try (var validationStream = csvParser.parseSafe(csvStream)) {
            validationStream.forEach(validationResult -> {
                stats.incrementTotal();

                // If validation failed, log errors and continue
                if (validationResult.hasErrors()) {
                    stats.incrementFailed();
                    long rowNumber = validationResult.errors().get(0).rowNumber();
                    stats.addValidationErrors(rowNumber, validationResult.errors());

                    log.warn("Row {} failed validation with {} errors:",
                            rowNumber, validationResult.errors().size());
                    for (var error : validationResult.errors()) {
                        log.warn("  - {}", error);
                    }
                    return; // Continue to next record
                }

                // Process valid record
                validationResult.record().ifPresent(record -> {
                    try {
                        processImportRecord(record, speakerCache, talkBatch, stats);

                        // Batch insert talks when batch is full
                        if (talkBatch.size() >= 1000) {
                            saveBatch(talkBatch);
                            talkBatch.clear();
                            logProgress(importId, stats);
                        }

                    } catch (Exception e) {
                        stats.incrementFailed();
                        log.error("Failed to process record at line {}: {} - {}",
                                stats.getTotal(), e.getClass().getSimpleName(), e.getMessage(), e);
                    }
                });
            });

            // Save final batch
            if (!talkBatch.isEmpty()) {
                log.info("Saving final batch of {} talks", talkBatch.size());
                saveBatch(talkBatch);
                logProgress(importId, stats);
            }

        } catch (Exception e) {
            log.error("Stream processing failed", e);
            throw new RuntimeException("CSV stream processing failed", e);
        }

        // Log validation error summary
        if (stats.getValidationErrorCount() > 0) {
            log.warn("Import {} completed with {} validation errors across {} rows",
                    importId, stats.getValidationErrorCount(), stats.getFailed());
            logValidationErrorSummary(stats);
        }

        log.info("Import {} completed: Total={}, Successful={}, Failed={}, Skipped={}",
                importId, stats.getTotal(), stats.getSuccessful(), stats.getFailed(), stats.getSkipped());

        return stats;
    }

    private void processImportRecord(CsvImportRecord record,
                                     Map<String, Speaker> speakerCache,
                                     List<TedTalk> talkBatch,
                                     ImportStatistics stats) {
        Speaker speaker = record.speaker();
        TedTalk talk = record.talk();

        log.debug("Processing record: Speaker={}, Talk={}", speaker.getName(), talk.getTitle());

        // Check if speaker already exists or is in cache
        Speaker existingSpeaker = speakerCache.get(speaker.getName());
        if (existingSpeaker == null) {
            existingSpeaker = speakerRepository.findByName(speaker.getName())
                    .orElse(null);

            if (existingSpeaker == null) {
                // New speaker - save to database
                log.debug("Saving new speaker: {}", speaker.getName());
                existingSpeaker = speakerRepository.save(speaker);
                log.debug("Speaker saved with ID: {}", existingSpeaker.getId());

            } else {
                log.debug("Speaker already exists: {} with ID: {}", existingSpeaker.getName(), existingSpeaker.getId());
            }

            speakerCache.put(existingSpeaker.getName(), existingSpeaker);
        }

        // Check if talk already exists
        boolean talkExists = tedTalkRepository.existsByTitleAndSpeakerId(
                talk.getTitle(), existingSpeaker.getId());

        log.debug("Talk '{}' by speaker {} exists: {}", talk.getTitle(), existingSpeaker.getName(), talkExists);

        if (!talkExists) {
            // Create new talk with existing speaker
            TedTalk newTalk = TedTalk.create(
                    talk.getTitle(),
                    existingSpeaker,
                    talk.getDate(),
                    talk.getViews(),
                    talk.getLikes(),
                    talk.getLink()
            );

            talkBatch.add(newTalk);
            log.debug("Added talk to batch: {} (batch size: {})", talk.getTitle(), talkBatch.size());
            stats.incrementSuccessful();  // Only count successful talk imports
        } else {
            stats.incrementSkipped();
            log.debug("Skipping duplicate talk: {} by {}", talk.getTitle(), existingSpeaker.getName());
        }
    }

    private void saveBatch(List<TedTalk> talks) {
        if (!talks.isEmpty()) {
            log.info("Saving batch of {} talks to database", talks.size());
            try {
                tedTalkRepository.saveAll(talks);
                log.info("Successfully saved {} talks to database", talks.size());
            } catch (Exception e) {
                log.error("Failed to save batch of {} talks: {}", talks.size(), e.getMessage(), e);
                throw e;
            }
        }
    }

    private void logProgress(String importId, ImportStatistics stats) {
        log.info("Import {}: Processed {}/{} records ({} successful, {} failed, {} skipped)",
                importId,
                stats.getTotal(),
                stats.getTotal(),
                stats.getSuccessful(),
                stats.getFailed(),
                stats.getSkipped());
    }

    private void logValidationErrorSummary(ImportStatistics stats) {
        var errors = stats.getAllValidationErrors();
        var errorsByType = errors.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ValidationError::type,
                        java.util.stream.Collectors.counting()
                ));

        log.warn("Validation error breakdown:");
        errorsByType.forEach((type, count) ->
                log.warn("  - {}: {} occurrences", type, count)
        );
    }
}

