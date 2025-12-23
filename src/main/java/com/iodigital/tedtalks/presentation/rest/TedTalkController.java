package com.iodigital.tedtalks.presentation.rest;

import com.iodigital.tedtalks.application.dto.TedTalkDto;
import com.iodigital.tedtalks.presentation.rest.request.CreateTedTalkRequest;
import com.iodigital.tedtalks.presentation.rest.request.UpdateTedTalkRequest;
import com.iodigital.tedtalks.application.service.TedTalkService;
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
@RequestMapping("/api/v1/tedtalks")
@Tag(name = "TED Talks", description = "TED Talks CRUD Operations")
@Slf4j
public class TedTalkController {

    private final TedTalkService tedTalkService;

    public TedTalkController(TedTalkService tedTalkService) {
        this.tedTalkService = tedTalkService;
    }

    @GetMapping
    @Operation(summary = "Get all TED talks")
    public ResponseEntity<List<TedTalkDto>> getAllTedTalks() {
        log.debug("Fetching all TED talks");
        List<TedTalkDto> talks = tedTalkService.getAllTedTalks();
        return ResponseEntity.ok(talks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get TED talk by ID")
    public ResponseEntity<TedTalkDto> getTedTalkById(@PathVariable String id) {
        log.debug("Fetching TED talk by id: {}", id);
        return tedTalkService.getTedTalkById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "TED talk not found with id: " + id
                ));
    }

    @GetMapping("/speaker/{speakerName}")
    @Operation(summary = "Get TED talks by speaker name")
    public ResponseEntity<List<TedTalkDto>> getTedTalksBySpeaker(
            @PathVariable String speakerName) {
        log.debug("Fetching TED talks by speaker: {}", speakerName);
        List<TedTalkDto> talks = tedTalkService.getTedTalksBySpeakerName(speakerName);
        return ResponseEntity.ok(talks);
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Get TED talks by year")
    public ResponseEntity<List<TedTalkDto>> getTedTalksByYear(
            @PathVariable int year) {
        log.debug("Fetching TED talks by year: {}", year);
        List<TedTalkDto> talks = tedTalkService.getTedTalksByYear(year);
        return ResponseEntity.ok(talks);
    }

    @GetMapping("/search")
    @Operation(summary = "Search TED talks by title")
    public ResponseEntity<List<TedTalkDto>> searchByTitle(
            @RequestParam String title) {
        log.debug("Searching TED talks by title: {}", title);
        List<TedTalkDto> talks = tedTalkService.searchByTitle(title);
        return ResponseEntity.ok(talks);
    }

    @GetMapping("/count")
    @Operation(summary = "Get total count of TED talks")
    public ResponseEntity<Long> getCount() {
        log.debug("Fetching TED talks count");
        long count = tedTalkService.count();
        return ResponseEntity.ok(count);
    }

    @PostMapping
    @Operation(summary = "Create a new TED talk")
    public ResponseEntity<TedTalkDto> createTedTalk(
            @Valid @RequestBody CreateTedTalkRequest request) {
        log.info("Creating new TED talk: {}", request.title());
        TedTalkDto created = tedTalkService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing TED talk")
    public ResponseEntity<TedTalkDto> updateTedTalk(
            @PathVariable String id,
            @Valid @RequestBody UpdateTedTalkRequest request) {
        log.info("Updating TED talk with id: {}", id);
        TedTalkDto updated = tedTalkService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a TED talk")
    public ResponseEntity<Void> deleteTedTalk(@PathVariable String id) {
        log.info("Deleting TED talk with id: {}", id);
        tedTalkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

