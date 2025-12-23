package com.iodigital.tedtalks.application.service;

import com.iodigital.tedtalks.application.dto.SpeakerInfluenceDto;
import com.iodigital.tedtalks.application.dto.TedTalkDto;
import com.iodigital.tedtalks.application.port.TedTalkAnalyzer;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.repository.TedTalkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class InfluenceAnalysisService implements TedTalkAnalyzer {

    private final TedTalkRepository repository;
    private final Executor analysisExecutor;
    private final CacheManager cacheManager;

    public InfluenceAnalysisService(TedTalkRepository repository,
                                    @Qualifier("analysisExecutor") Executor analysisExecutor,
                                    CacheManager cacheManager) {
        this.repository = repository;
        this.analysisExecutor = analysisExecutor;
        this.cacheManager = cacheManager;
    }

    @Override
    @Cacheable(value = "topSpeakers", key = "#limit")
    public List<SpeakerInfluenceDto> getTopInfluentialSpeakers(int limit) {
        log.info("Calculating top {} influential speakers", limit);
        long startTime = System.currentTimeMillis();

        // Step 1: Stream data from DB (memory efficient)
        Map<String, List<TedTalk>> talksBySpeaker;
        try (Stream<TedTalk> talkStream = repository.streamAll()) {
            talksBySpeaker = talkStream
                    .collect(Collectors.groupingBy(talk -> talk.getSpeaker().getName()));
        }

        log.debug("Grouped {} speakers in {} ms", talksBySpeaker.size(),
                System.currentTimeMillis() - startTime);

        // Step 2: Process speakers in parallel using async for maximum performance
        List<SpeakerInfluenceDto> result = talksBySpeaker.entrySet().parallelStream()
                .map(entry -> calculateSpeakerInfluenceAsync(entry.getKey(), entry.getValue()))
                .map(CompletableFuture::join) // Wait for all async operations
                .sorted(Comparator.comparingDouble(SpeakerInfluenceDto::totalInfluenceScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Calculated top {} speakers in {} ms", limit,
                System.currentTimeMillis() - startTime);

        return result;
    }

    /**
     * Alternative version for truly async API responses (non-blocking)
     */
    public CompletableFuture<List<SpeakerInfluenceDto>> getTopInfluentialSpeakersAsync(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Async calculation of top {} influential speakers", limit);
            return getTopInfluentialSpeakers(limit);
        }, analysisExecutor);
    }

    @Override
    @Cacheable("mostInfluentialPerYear")
    public Map<Integer, TedTalkDto> getMostInfluentialTalkPerYear() {
        log.info("Calculating most influential talk per year");

        // Use streaming to avoid loading all data into memory at once
        try (Stream<TedTalk> talkStream = repository.streamAll()) {
            return talkStream
                    .collect(Collectors.groupingBy(
                            talk -> talk.getDate().year(),
                            Collectors.maxBy(
                                    Comparator.comparingDouble(TedTalk::calculateInfluenceScore)
                            )
                    ))
                    .entrySet().stream()
                    .filter(entry -> entry.getValue().isPresent())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> TedTalkDto.fromDomain(entry.getValue().get())
                    ));
        }
    }

    @Override
    @Cacheable(value = "speakerAnalysis", key = "#speakerName.toLowerCase()")
    public Optional<SpeakerInfluenceDto> analyzeSpeaker(String speakerName) {
        log.info("Analyzing speaker: {} (cache miss - calculating)", speakerName);

        // Use repository method to query specific speaker instead of loading all data
        List<TedTalk> talks = repository.findBySpeakerName(speakerName);

        if (talks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(calculateSpeakerInfluence(speakerName, talks));
    }

    /**
     * Async version for parallel processing of speaker statistics
     */
    private CompletableFuture<SpeakerInfluenceDto> calculateSpeakerInfluenceAsync(
            String speaker, List<TedTalk> talks) {
        return CompletableFuture.supplyAsync(() ->
            calculateSpeakerInfluence(speaker, talks), analysisExecutor);
    }

    /**
     * Optimized calculation using single-pass processing
     */
    private SpeakerInfluenceDto calculateSpeakerInfluence(String speaker, List<TedTalk> talks) {
        // Single pass through data for better performance
        long totalViews = 0;
        long totalLikes = 0;
        double totalInfluence = 0;
        int firstYear = Integer.MAX_VALUE;
        int lastYear = Integer.MIN_VALUE;

        for (TedTalk talk : talks) {
            totalViews += talk.getViews().value();
            totalLikes += talk.getLikes().value();
            totalInfluence += talk.calculateInfluenceScore();

            int year = talk.getDate().year();
            if (year < firstYear) firstYear = year;
            if (year > lastYear) lastYear = year;
        }

        double averageInfluence = totalInfluence / talks.size();

        return new SpeakerInfluenceDto(
                speaker,
                talks.size(),
                totalViews,
                totalLikes,
                averageInfluence,
                totalInfluence,
                firstYear == Integer.MAX_VALUE ? 0 : firstYear,
                lastYear == Integer.MIN_VALUE ? 0 : lastYear
        );
    }

    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void refreshCache() {
        log.info("Refreshing analysis cache");

        // Clear analysis caches
        Cache topSpeakersCache = cacheManager.getCache("topSpeakers");
        if (topSpeakersCache != null) {
            topSpeakersCache.clear();
            log.debug("Cleared topSpeakers cache");
        }

        Cache mostInfluentialCache = cacheManager.getCache("mostInfluentialPerYear");
        if (mostInfluentialCache != null) {
            mostInfluentialCache.clear();
            log.debug("Cleared mostInfluentialPerYear cache");
        }

        // Clear new critical caches
        Cache allTedTalksCache = cacheManager.getCache("allTedTalks");
        if (allTedTalksCache != null) {
            allTedTalksCache.clear();
            log.debug("Cleared allTedTalks cache");
        }

        Cache allSpeakersCache = cacheManager.getCache("allSpeakers");
        if (allSpeakersCache != null) {
            allSpeakersCache.clear();
            log.debug("Cleared allSpeakers cache");
        }

        // Clear speaker analysis cache
        Cache speakerAnalysisCache = cacheManager.getCache("speakerAnalysis");
        if (speakerAnalysisCache != null) {
            speakerAnalysisCache.clear();
            log.debug("Cleared speakerAnalysis cache");
        }

        log.info("Analysis cache refreshed successfully");
    }

}

