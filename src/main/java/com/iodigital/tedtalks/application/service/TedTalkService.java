package com.iodigital.tedtalks.application.service;

import com.iodigital.tedtalks.application.dto.TedTalkDto;
import com.iodigital.tedtalks.presentation.rest.request.CreateTedTalkRequest;
import com.iodigital.tedtalks.presentation.rest.request.UpdateTedTalkRequest;
import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.model.valueobject.*;
import com.iodigital.tedtalks.domain.repository.TedTalkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TedTalkService {

    private final TedTalkRepository tedTalkRepository;

    public TedTalkService(TedTalkRepository tedTalkRepository) {
        this.tedTalkRepository = tedTalkRepository;
    }

    @Cacheable("allTedTalks")
    public List<TedTalkDto> getAllTedTalks() {
        log.info("Fetching all TED talks from database (cache miss)");
        return tedTalkRepository.findAll().stream()
                .map(TedTalkDto::fromDomain)
                .toList();
    }

    public Optional<TedTalkDto> getTedTalkById(String id) {
        log.debug("Fetching TED talk by id: {}", id);
        try {
            TedTalkId tedTalkId = TedTalkId.fromString(id);
            return tedTalkRepository.findById(tedTalkId)
                    .map(TedTalkDto::fromDomain);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid TedTalk ID format: {}", id);
            return Optional.empty();
        }
    }

    public List<TedTalkDto> getTedTalksBySpeakerName(String speakerName) {
        log.debug("Fetching TED talks by speaker: {}", speakerName);
        return tedTalkRepository.findBySpeakerName(speakerName).stream()
                .map(TedTalkDto::fromDomain)
                .toList();
    }

    public List<TedTalkDto> getTedTalksByYear(int year) {
        log.debug("Fetching TED talks by year: {}", year);
        return tedTalkRepository.findByYear(year).stream()
                .map(TedTalkDto::fromDomain)
                .toList();
    }

    public List<TedTalkDto> searchByTitle(String titleKeyword) {
        log.debug("Searching TED talks by title keyword: {}", titleKeyword);
        return tedTalkRepository.findAll().stream()
                .filter(talk -> talk.getTitle().toLowerCase().contains(titleKeyword.toLowerCase()))
                .map(TedTalkDto::fromDomain)
                .toList();
    }

    public long count() {
        return tedTalkRepository.count();
    }

    /**
     * Create a new TED talk
     */
    @CacheEvict(value = {"allTedTalks", "topSpeakers", "mostInfluentialPerYear", "speakerAnalysis"}, allEntries = true)
    public TedTalkDto create(CreateTedTalkRequest request) {
        log.info("Creating new TED talk: {} (will invalidate caches)", request.title());

        // Parse and validate date
        TalkDate talkDate = parseTalkDate(request.date());

        // Create domain object
        TedTalk tedTalk = TedTalk.create(
                request.title(),
                Speaker.create(request.speaker(), null),
                talkDate,
                Views.of(request.views()),
                Likes.of(request.likes()),
                Link.of(request.link())
        );

        // Save to repository
        TedTalk saved = tedTalkRepository.save(tedTalk);

        log.info("TED talk created successfully: {}", saved.getTitle());
        return TedTalkDto.fromDomain(saved);
    }

    /**
     * Update an existing TED talk
     */
    @CacheEvict(value = {"allTedTalks", "topSpeakers", "mostInfluentialPerYear", "speakerAnalysis"}, allEntries = true)
    public TedTalkDto update(String id, UpdateTedTalkRequest request) {
        log.info("Updating TED talk with id: {} (will invalidate caches)", id);

        // Find existing talk
        TedTalkId tedTalkId = parseTedTalkId(id);
        TedTalk existing = tedTalkRepository.findById(tedTalkId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "TED talk not found with id: " + id
                ));

        // Parse and validate date
        TalkDate talkDate = parseTalkDate(request.date());

        // Create updated domain object
        TedTalk updated = TedTalk.create(
                request.title(),
                Speaker.create(request.speaker(), null),
                talkDate,
                Views.of(request.views()),
                Likes.of(request.likes()),
                Link.of(request.link())
        );

        // Save to repository
        TedTalk saved = tedTalkRepository.save(updated);

        log.info("TED talk updated successfully: {}", saved.getTitle());
        return TedTalkDto.fromDomain(saved);
    }

    /**
     * Delete a TED talk by ID
     */
    @CacheEvict(value = {"allTedTalks", "topSpeakers", "mostInfluentialPerYear", "speakerAnalysis"}, allEntries = true)
    public void delete(String id) {
        log.info("Deleting TED talk with id: {} (will invalidate caches)", id);

        TedTalkId tedTalkId = parseTedTalkId(id);

        // Verify it exists before deleting
        if (tedTalkRepository.findById(tedTalkId).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "TED talk not found with id: " + id
            );
        }

        tedTalkRepository.delete(tedTalkId);
        log.info("TED talk deleted successfully: {}", id);
    }

    // Helper methods

    private TedTalkId parseTedTalkId(String id) {
        try {
            return TedTalkId.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid TED talk ID format: " + id
            );
        }
    }

    private TalkDate parseTalkDate(String dateString) {
        try {
            return TalkDate.fromString(dateString);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid date format: " + dateString + ". Expected format: 'December 2021'"
            );
        }
    }
}

