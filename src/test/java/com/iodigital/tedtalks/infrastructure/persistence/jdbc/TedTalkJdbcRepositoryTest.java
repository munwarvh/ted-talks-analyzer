package com.iodigital.tedtalks.infrastructure.persistence.jdbc;

import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(TedTalkH2Repository.class)
@DisplayName("TedTalk JDBC Repository Integration Tests")
class TedTalkJdbcRepositoryTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TedTalkH2Repository repository;

    @Test
    @DisplayName("Should save a new TED talk to database")
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldSaveTedTalk() {
        // Given
        TedTalk talk = createTestTalk("Test Talk", "Test Speaker", "January 2020");

        // When
        repository.save(talk);

        // Then
        long count = repository.count();
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should save multiple talks in batch")
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldSaveAllTalks() {
        // Given
        List<TedTalk> talks = List.of(
                createTestTalk("Talk 1", "Speaker 1", "January 2020"),
                createTestTalk("Talk 2", "Speaker 2", "February 2020"),
                createTestTalk("Talk 3", "Speaker 3", "March 2020")
        );

        // When
        repository.saveAll(talks);

        // Then
        assertThat(repository.count()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should find all talks")
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldFindAllTalks() {
        // Given
        repository.saveAll(List.of(
                createTestTalk("Talk 1", "Speaker 1", "January 2020"),
                createTestTalk("Talk 2", "Speaker 2", "February 2020")
        ));

        // When
        List<TedTalk> talks = repository.findAll();

        // Then
        assertThat(talks).hasSize(2);
        assertThat(talks).extracting(TedTalk::getTitle)
                .containsExactlyInAnyOrder("Talk 1", "Talk 2");
    }

    @Test
    @DisplayName("Should find talks by year")
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldFindTalksByYear() {
        // Given
        repository.saveAll(List.of(
                createTestTalk("Talk 2020", "Speaker 1", "January 2020"),
                createTestTalk("Talk 2021", "Speaker 2", "January 2021"),
                createTestTalk("Another 2020", "Speaker 3", "December 2020")
        ));

        // When
        List<TedTalk> talks2020 = repository.findByYear(2020);

        // Then
        assertThat(talks2020).hasSize(2);
        assertThat(talks2020).extracting(TedTalk::getTitle)
                .containsExactlyInAnyOrder("Talk 2020", "Another 2020");
    }

    @Test
    @DisplayName("Should find talks by speaker name")
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldFindTalksBySpeakerName() {
        // Given
        String speakerName = "Hans Rosling";
        repository.saveAll(List.of(
                createTestTalk("Talk 1", speakerName, "January 2020"),
                createTestTalk("Talk 2", speakerName, "February 2020"),
                createTestTalk("Talk 3", "Another Speaker", "March 2020")
        ));

        // When
        List<TedTalk> talks = repository.findBySpeakerName(speakerName);

        // Then
        assertThat(talks).hasSize(2);
        assertThat(talks).allMatch(talk -> talk.getSpeaker().getName().equals(speakerName));
    }

    @Test
    @DisplayName("Should return correct count")
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldReturnCorrectCount() {
        // Given
        repository.saveAll(List.of(
                createTestTalk("Talk 1", "Speaker 1", "January 2020"),
                createTestTalk("Talk 2", "Speaker 2", "February 2020"),
                createTestTalk("Talk 3", "Speaker 3", "March 2020"),
                createTestTalk("Talk 4", "Speaker 4", "April 2020")
        ));

        // When
        long count = repository.count();

        // Then
        assertThat(count).isEqualTo(4L);
    }

    @Test
    @DisplayName("Should handle duplicate talks with upsert")
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldHandleDuplicatesWithUpsert() {
        // Given
        TedTalk talk1 = createTestTalk("Same Talk", "Same Speaker", "January 2020", 1000000L, 50000L);
        repository.save(talk1);

        // When - Save same talk with different stats
        TedTalk talk2 = createTestTalk("Same Talk", "Same Speaker", "January 2020", 2000000L, 75000L);
        repository.save(talk2);

        // Then - Should have only 1 record (updated)
        assertThat(repository.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return empty list when no talks found by year")
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldReturnEmptyListWhenNoTalksFoundByYear() {
        // When
        List<TedTalk> talks = repository.findByYear(2025);

        // Then
        assertThat(talks).isEmpty();
    }

    // Helper methods

    private TedTalk createTestTalk(String title, String speakerName, String date) {
        return createTestTalk(title, speakerName, date, 1000000L, 50000L);
    }

    private TedTalk createTestTalk(String title, String speakerName, String date, long views, long likes) {
        Speaker speaker = Speaker.create(speakerName, null);
        TalkDate talkDate = TalkDate.fromString(date);
        return TedTalk.create(
                title,
                speaker,
                talkDate,
                Views.of(views),
                Likes.of(likes),
                Link.of("https://ted.com/talks/" + title.toLowerCase().replace(" ", "-"))
        );
    }
}

