package com.iodigital.tedtalks.domain.repository;

import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.valueobject.SpeakerId;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface SpeakerRepository {

    /**
     * Find all speakers
     */
    List<Speaker> findAll();

    /**
     * Stream all speakers efficiently without loading all into memory at once.
     */
    Stream<Speaker> streamAll();

    /**
     * Find speaker by ID
     */
    Optional<Speaker> findById(SpeakerId id);

    /**
     * Find speaker by name
     */
    Optional<Speaker> findByName(String name);

    /**
     * Find speakers by name pattern (case-insensitive)
     */
    List<Speaker> findByNameContaining(String namePattern);

    /**
     * Save speaker
     */
    Speaker save(Speaker speaker);

    /**
     * Save all speakers
     */
    void saveAll(List<Speaker> speakers);

    /**
     * Delete speaker by ID
     */
    void delete(SpeakerId id);

    /**
     * Check if speaker exists by name
     */
    boolean existsByName(String name);

    /**
     * Count all speakers
     */
    long count();
}

