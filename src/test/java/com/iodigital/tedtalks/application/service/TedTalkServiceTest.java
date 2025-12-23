package com.iodigital.tedtalks.application.service;

import com.iodigital.tedtalks.application.dto.TedTalkDto;
import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.model.valueobject.*;
import com.iodigital.tedtalks.domain.repository.TedTalkRepository;
import com.iodigital.tedtalks.presentation.rest.request.CreateTedTalkRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TedTalk Service Unit Tests")
class TedTalkServiceTest {

    @Mock
    private TedTalkRepository tedTalkRepository;

    @InjectMocks
    private TedTalkService tedTalkService;

    private TedTalk testTalk;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Should return all TED talks from repository")
    void shouldReturnAllTedTalks() {
        // Given
        TedTalk talk2 = TedTalk.create(
                "Second Talk",
                Speaker.create("Another Speaker", null),
                TalkDate.fromString("February 2020"),
                Views.of(500000L),
                Likes.of(25000L),
                Link.of("https://ted.com/talks/second")
        );
        when(tedTalkRepository.findAll()).thenReturn(List.of(testTalk, talk2));

        // When
        List<TedTalkDto> result = tedTalkService.getAllTedTalks();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Test Talk");
        assertThat(result.get(1).title()).isEqualTo("Second Talk");
        verify(tedTalkRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no talks exist")
    void shouldReturnEmptyListWhenNoTalks() {
        // Given
        when(tedTalkRepository.findAll()).thenReturn(List.of());

        // When
        List<TedTalkDto> result = tedTalkService.getAllTedTalks();

        // Then
        assertThat(result).isEmpty();
        verify(tedTalkRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should create new TED talk successfully")
    void shouldCreateTedTalk() {
        // Given
        CreateTedTalkRequest request = new CreateTedTalkRequest(
                "New Talk",
                "New Speaker",
                "March 2021",
                100000L,
                5000L,
                "https://ted.com/talks/new"
        );
        when(tedTalkRepository.save(any(TedTalk.class))).thenReturn(testTalk);

        // When
        TedTalkDto result = tedTalkService.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test Talk");
        verify(tedTalkRepository, times(1)).save(any(TedTalk.class));
    }

    @Test
    @DisplayName("Should return talks by year")
    void shouldReturnTalksByYear() {
        // Given
        when(tedTalkRepository.findByYear(2020)).thenReturn(List.of(testTalk));

        // When
        List<TedTalkDto> result = tedTalkService.getTedTalksByYear(2020);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Test Talk");
        verify(tedTalkRepository, times(1)).findByYear(2020);
    }

    @Test
    @DisplayName("Should return talks by speaker name")
    void shouldReturnTalksBySpeakerName() {
        // Given
        String speakerName = "Test Speaker";
        when(tedTalkRepository.findBySpeakerName(speakerName)).thenReturn(List.of(testTalk));

        // When
        List<TedTalkDto> result = tedTalkService.getTedTalksBySpeakerName(speakerName);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).speaker().getName()).isEqualTo(speakerName);
        verify(tedTalkRepository, times(1)).findBySpeakerName(speakerName);
    }

    @Test
    @DisplayName("Should return talk by ID")
    void shouldReturnTalkById() {
        // Given
        String idString = "550e8400-e29b-41d4-a716-446655440000";
        TedTalkId id = TedTalkId.fromString(idString);
        when(tedTalkRepository.findById(id)).thenReturn(Optional.of(testTalk));

        // When
        Optional<TedTalkDto> result = tedTalkService.getTedTalkById(idString);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("Test Talk");
        verify(tedTalkRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should return empty when talk not found by ID")
    void shouldReturnEmptyWhenTalkNotFound() {
        // Given
        String idString = "550e8400-e29b-41d4-a716-446655440099";
        TedTalkId id = TedTalkId.fromString(idString);
        when(tedTalkRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<TedTalkDto> result = tedTalkService.getTedTalkById(idString);

        // Then
        assertThat(result).isEmpty();
        verify(tedTalkRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should return count of all talks")
    void shouldReturnCount() {
        // Given
        when(tedTalkRepository.count()).thenReturn(5L);

        // When
        long count = tedTalkService.count();

        // Then
        assertThat(count).isEqualTo(5L);
        verify(tedTalkRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent talk")
    void shouldThrowExceptionWhenDeletingNonExistentTalk() {
        // Given
        String id = "550e8400-e29b-41d4-a716-446655440099";
        TedTalkId tedTalkId = TedTalkId.fromString(id);
        when(tedTalkRepository.findById(tedTalkId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> tedTalkService.delete(id))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("TED talk not found");
    }
}

