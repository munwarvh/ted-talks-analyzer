package com.iodigital.tedtalks.application.service;

import com.iodigital.tedtalks.application.dto.TedTalkDto;
import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.model.valueobject.*;
import com.iodigital.tedtalks.domain.repository.TedTalkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Caching Integration Tests")
class CachingIntegrationTest {

    @Autowired
    private TedTalkService tedTalkService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private TedTalkRepository tedTalkRepository;

    private TedTalk testTalk;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });

        // Setup test data
        Speaker speaker = Speaker.create("Test Speaker", null);
        TalkDate date = TalkDate.fromString("January 2020");
        testTalk = TedTalk.create(
                "Test Talk",
                speaker,
                date,
                Views.of(1000000L),
                Likes.of(50000L),
                Link.of("https://ted.com/talks/test")
        );
    }

    @Test
    @DisplayName("Should cache getAllTedTalks result")
    void shouldCacheGetAllTedTalks() {
        // Given
        when(tedTalkRepository.findAll()).thenReturn(List.of(testTalk));

        // When - First call (cache miss)
        List<TedTalkDto> result1 = tedTalkService.getAllTedTalks();

        // Then - Repository called once
        verify(tedTalkRepository, times(1)).findAll();
        assertThat(result1).hasSize(1);

        // When - Second call (cache hit)
        List<TedTalkDto> result2 = tedTalkService.getAllTedTalks();

        // Then - Repository still called only once (result from cache)
        verify(tedTalkRepository, times(1)).findAll();
        assertThat(result2).hasSize(1);
        assertThat(result2).isEqualTo(result1);
    }

    @Test
    @DisplayName("Should evict cache when creating new talk")
    void shouldEvictCacheWhenCreatingTalk() {
        // Given - Fill cache
        when(tedTalkRepository.findAll()).thenReturn(List.of(testTalk));
        tedTalkService.getAllTedTalks();
        verify(tedTalkRepository, times(1)).findAll();

        // When - Create new talk (should evict cache)
        when(tedTalkRepository.save(any())).thenReturn(testTalk);
        tedTalkService.create(new com.iodigital.tedtalks.presentation.rest.request.CreateTedTalkRequest(
                "New Talk",
                "New Speaker",
                "March 2021",
                100000L,
                5000L,
                "https://ted.com/talks/new"
        ));

        // Then - Next call should hit repository again (cache was cleared)
        when(tedTalkRepository.findAll()).thenReturn(List.of(testTalk, testTalk));
        tedTalkService.getAllTedTalks();
        verify(tedTalkRepository, times(2)).findAll();
    }

    @Test
    @DisplayName("Should verify cache manager has required caches")
    void shouldHaveRequiredCaches() {
        // Then - Check for core caches (allSpeakers only exists when SpeakerService is invoked)
        assertThat(cacheManager.getCacheNames())
                .contains("allTedTalks", "topSpeakers",
                         "mostInfluentialPerYear", "speakerAnalysis");
    }

    @Test
    @DisplayName("Should be able to clear specific cache")
    void shouldClearSpecificCache() {
        // Given
        when(tedTalkRepository.findAll()).thenReturn(List.of(testTalk));
        tedTalkService.getAllTedTalks();

        var cache = cacheManager.getCache("allTedTalks");
        assertThat(cache).isNotNull();

        // When
        cache.clear();

        // Then - Next call should hit repository (cache cleared)
        tedTalkService.getAllTedTalks();
        verify(tedTalkRepository, times(2)).findAll();
    }
}

