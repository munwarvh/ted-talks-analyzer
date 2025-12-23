package com.iodigital.tedtalks.domain.repository;

import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.model.valueobject.SpeakerId;
import com.iodigital.tedtalks.domain.model.valueobject.TedTalkId;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.function.Consumer;

public interface TedTalkRepository {

    List<TedTalk> findAll();

    /**
     * Stream all talks efficiently without loading all into memory at once.
     * The stream should be closed after use to free resources.
     */
    Stream<TedTalk> streamAll();

    /**
     * Process talks in batches to avoid memory issues.
     * @param batchSize number of records to process at a time
     * @param processor consumer to process each batch
     */
    void processBatches(int batchSize, Consumer<List<TedTalk>> processor);

    Optional<TedTalk> findById(TedTalkId id);

    List<TedTalk> findBySpeakerId(SpeakerId speakerId);

    List<TedTalk> findByYear(int year);

    List<TedTalk> findBySpeakerName(String speakerName);

    TedTalk save(TedTalk talk);

    void saveAll(List<TedTalk> talks);

    void delete(TedTalkId id);

    boolean existsByTitleAndSpeakerId(String title, SpeakerId speakerId);

    long count();
}