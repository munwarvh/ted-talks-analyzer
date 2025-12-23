package com.iodigital.tedtalks.presentation.rest;

import com.iodigital.tedtalks.application.dto.SpeakerDto;
import com.iodigital.tedtalks.application.dto.TedTalkDto;
import com.iodigital.tedtalks.presentation.rest.request.CreateSpeakerRequest;
import com.iodigital.tedtalks.presentation.rest.request.UpdateSpeakerRequest;
import com.iodigital.tedtalks.application.service.SpeakerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/speakers")
@Tag(name = "Speakers", description = "Speaker Management Operations")
@Slf4j
public class SpeakerController {

    private final SpeakerService speakerService;

    public SpeakerController(SpeakerService speakerService) {
        this.speakerService = speakerService;
    }

    @GetMapping
    @Operation(summary = "Get all speakers")
    public ResponseEntity<List<SpeakerDto>> getAllSpeakers() {
        log.debug("Fetching all speakers");
        List<SpeakerDto> speakers = speakerService.getAllSpeakers();
        return ResponseEntity.ok(speakers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get speaker by ID")
    public ResponseEntity<SpeakerDto> getSpeakerById(@PathVariable String id) {
        log.debug("Fetching speaker by id: {}", id);
        return speakerService.getSpeakerById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Speaker not found with id: " + id
                ));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get speaker by name")
    public ResponseEntity<SpeakerDto> getSpeakerByName(@PathVariable String name) {
        log.debug("Fetching speaker by name: {}", name);
        return speakerService.getSpeakerByName(name)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Speaker not found with name: " + name
                ));
    }

    @GetMapping("/search")
    @Operation(summary = "Search speakers by name pattern")
    public ResponseEntity<List<SpeakerDto>> searchSpeakers(
            @RequestParam String name) {
        log.debug("Searching speakers by name: {}", name);
        List<SpeakerDto> speakers = speakerService.searchSpeakers(name);
        return ResponseEntity.ok(speakers);
    }

    @GetMapping("/{id}/talks")
    @Operation(summary = "Get all talks by a speaker")
    public ResponseEntity<List<TedTalkDto>> getSpeakerTalks(
            @PathVariable String id) {
        log.debug("Fetching talks for speaker: {}", id);
        List<TedTalkDto> talks = speakerService.getSpeakerTalks(id);
        return ResponseEntity.ok(talks);
    }

    @GetMapping("/count")
    @Operation(summary = "Get total count of speakers")
    public ResponseEntity<Long> getCount() {
        log.debug("Fetching speakers count");
        long count = speakerService.count();
        return ResponseEntity.ok(count);
    }

    @PostMapping
    @Operation(summary = "Create a new speaker")
    public ResponseEntity<SpeakerDto> createSpeaker(
            @Valid @RequestBody CreateSpeakerRequest request) {
        log.info("Creating new speaker: {}", request.name());
        SpeakerDto created = speakerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing speaker")
    public ResponseEntity<SpeakerDto> updateSpeaker(
            @PathVariable String id,
            @Valid @RequestBody UpdateSpeakerRequest request) {
        log.info("Updating speaker with id: {}", id);
        SpeakerDto updated = speakerService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a speaker")
    public ResponseEntity<Void> deleteSpeaker(@PathVariable String id) {
        log.info("Deleting speaker with id: {}", id);
        speakerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

