package com.iodigital.tedtalks.application.service;

import com.iodigital.tedtalks.application.dto.SpeakerDto;
import com.iodigital.tedtalks.application.dto.TedTalkDto;
import com.iodigital.tedtalks.presentation.rest.request.CreateSpeakerRequest;
import com.iodigital.tedtalks.presentation.rest.request.UpdateSpeakerRequest;
import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.model.valueobject.SpeakerId;
import com.iodigital.tedtalks.domain.repository.SpeakerRepository;
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
public class SpeakerService {

    private final SpeakerRepository speakerRepository;
    private final TedTalkRepository tedTalkRepository;

    public SpeakerService(SpeakerRepository speakerRepository, TedTalkRepository tedTalkRepository) {
        this.speakerRepository = speakerRepository;
        this.tedTalkRepository = tedTalkRepository;
    }

    @Cacheable("allSpeakers")
    public List<SpeakerDto> getAllSpeakers() {
        log.info("Fetching all speakers from database (cache miss) - This may take 2-5 seconds due to N+1 queries");
        return speakerRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<SpeakerDto> getSpeakerById(String id) {
        log.debug("Fetching speaker by id: {}", id);
        try {
            SpeakerId speakerId = SpeakerId.fromString(id);
            return speakerRepository.findById(speakerId)
                    .map(this::toDto);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid Speaker ID format: {}", id);
            return Optional.empty();
        }
    }

    public Optional<SpeakerDto> getSpeakerByName(String name) {
        log.debug("Fetching speaker by name: {}", name);
        return speakerRepository.findByName(name)
                .map(this::toDto);
    }

    public List<SpeakerDto> searchSpeakers(String namePattern) {
        log.debug("Searching speakers by name pattern: {}", namePattern);
        return speakerRepository.findByNameContaining(namePattern).stream()
                .map(this::toDto)
                .toList();
    }

    public List<TedTalkDto> getSpeakerTalks(String speakerId) {
        log.debug("Fetching talks for speaker: {}", speakerId);
        try {
            SpeakerId id = SpeakerId.fromString(speakerId);
            return tedTalkRepository.findBySpeakerId(id).stream()
                    .map(TedTalkDto::fromDomain)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid Speaker ID format: {}", speakerId);
            return List.of();
        }
    }

    public long count() {
        return speakerRepository.count();
    }

    /**
     * Create a new speaker
     */
    @CacheEvict(value = {"allSpeakers", "topSpeakers", "speakerAnalysis"}, allEntries = true)
    public SpeakerDto create(CreateSpeakerRequest request) {
        log.info("Creating new speaker: {} (will invalidate caches)", request.name());

        // Check if speaker already exists
        Optional<Speaker> existing = speakerRepository.findByName(request.name());
        if (existing.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Speaker already exists with name: " + request.name()
            );
        }

        // Create domain object
        Speaker speaker = Speaker.create(request.name(), request.bio());

        // Save to repository
        Speaker saved = speakerRepository.save(speaker);

        log.info("Speaker created successfully: {}", saved.getName());
        return toDto(saved);
    }

    /**
     * Update an existing speaker
     */
    @CacheEvict(value = {"allSpeakers", "topSpeakers", "speakerAnalysis"}, allEntries = true)
    public SpeakerDto update(String id, UpdateSpeakerRequest request) {
        log.info("Updating speaker with id: {} (will invalidate caches)", id);

        // Find existing speaker
        SpeakerId speakerId = parseSpeakerId(id);
        Speaker existing = speakerRepository.findById(speakerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Speaker not found with id: " + id
                ));

        // Create updated domain object
        Speaker updated = Speaker.create(request.name(), request.bio());

        // Save to repository
        Speaker saved = speakerRepository.save(updated);

        log.info("Speaker updated successfully: {}", saved.getName());
        return toDto(saved);
    }

    /**
     * Delete a speaker by ID
     */
    @CacheEvict(value = {"allSpeakers", "topSpeakers", "speakerAnalysis"}, allEntries = true)
    public void delete(String id) {
        log.info("Deleting speaker with id: {} (will invalidate caches)", id);

        SpeakerId speakerId = parseSpeakerId(id);

        // Verify it exists before deleting
        if (speakerRepository.findById(speakerId).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Speaker not found with id: " + id
            );
        }

        // Check if speaker has talks
        List<TedTalk> talks = tedTalkRepository.findBySpeakerId(speakerId);
        if (!talks.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete speaker with existing talks. Speaker has " + talks.size() + " talk(s)."
            );
        }

        speakerRepository.delete(speakerId);
        log.info("Speaker deleted successfully: {}", id);
    }

    // Helper method

    private SpeakerId parseSpeakerId(String id) {
        try {
            return SpeakerId.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid speaker ID format: " + id
            );
        }
    }

    private SpeakerDto toDto(Speaker speaker) {
        // Calculate statistics for this speaker
        try {
            SpeakerId speakerId = speaker.getId();
            List<TedTalk> talks = tedTalkRepository.findBySpeakerId(speakerId);

            long totalTalks = talks.size();
            long totalViews = talks.stream()
                    .mapToLong(talk -> talk.getViews().value())
                    .sum();
            long totalLikes = talks.stream()
                    .mapToLong(talk -> talk.getLikes().value())
                    .sum();

            return SpeakerDto.fromDomain(speaker, totalTalks, totalViews, totalLikes);
        } catch (Exception e) {
            log.warn("Failed to calculate speaker statistics: {}", e.getMessage());
            return SpeakerDto.fromDomainBasic(speaker);
        }
    }
}

