package com.iodigital.tedtalks.presentation.rest;

import com.iodigital.tedtalks.application.dto.SpeakerInfluenceDto;
import com.iodigital.tedtalks.application.dto.TedTalkDto;
import com.iodigital.tedtalks.application.port.TedTalkAnalyzer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/analysis")
@Tag(name = "Analysis", description = "TedTalk Influence Analysis")
@Slf4j
public class AnalysisController {

    private final TedTalkAnalyzer analyzer;

    public AnalysisController(TedTalkAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @GetMapping("/speakers/top")
    @Operation(summary = "Get top influential speakers")
    public CompletableFuture<ResponseEntity<List<SpeakerInfluenceDto>>> getTopSpeakers(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {

        log.info("Fetching top {} influential speakers (async)", limit);

        return CompletableFuture.supplyAsync(() -> analyzer.getTopInfluentialSpeakers(limit))
                .thenApply(result -> {
                    log.debug("Analysis completed for {} speakers", result.size());
                    return ResponseEntity.ok(result);
                })
                .exceptionally(e -> {
                    log.error("Failed to analyze top speakers", e);
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Analysis failed: " + e.getMessage(),
                            e
                    );
                });
    }

    @GetMapping("/talks/most-influential-per-year")
    @Operation(summary = "Get most influential talk per year")
    public CompletableFuture<ResponseEntity<Map<Integer, TedTalkDto>>> getMostInfluentialPerYear() {

        log.info("Fetching most influential talk per year (async)");

        return CompletableFuture.supplyAsync(() -> analyzer.getMostInfluentialTalkPerYear())
                .thenApply(result -> {
                    log.debug("Analysis completed for {} years", result.size());
                    return ResponseEntity.ok(result);
                })
                .exceptionally(e -> {
                    log.error("Failed to analyze per-year influence", e);
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Analysis failed: " + e.getMessage(),
                            e
                    );
                });
    }

    @GetMapping("/speakers/{speaker}")
    @Operation(summary = "Analyze specific speaker")
    public ResponseEntity<SpeakerInfluenceDto> analyzeSpeaker(
            @PathVariable String speaker) {

        log.info("Analyzing speaker: {}", speaker);

        return analyzer.analyzeSpeaker(speaker)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Speaker not found: " + speaker
                ));
    }
}

