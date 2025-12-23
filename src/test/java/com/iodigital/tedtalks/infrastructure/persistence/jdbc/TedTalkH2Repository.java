package com.iodigital.tedtalks.infrastructure.persistence.jdbc;

import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.model.valueobject.*;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * H2-compatible repository for testing
 * Uses MERGE instead of ON CONFLICT
 */
@Repository
@Profile("test")
public class TedTalkH2Repository implements com.iodigital.tedtalks.domain.repository.TedTalkRepository {

    private static final String MERGE_SQL = """
        MERGE INTO ted_talks (
            title, author, date, talk_year, talk_month,
            views, likes, link, influence_score, created_at
        ) KEY(title, author)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private final JdbcTemplate jdbcTemplate;

    public TedTalkH2Repository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void saveAll(List<TedTalk> talks) {
        for (TedTalk talk : talks) {
            double influenceScore = talk.calculateInfluenceScore();
            jdbcTemplate.update(
                    MERGE_SQL,
                    talk.getTitle(),
                    talk.getSpeaker().getName(),
                    java.sql.Date.valueOf(talk.getDate().toLocalDate()),
                    talk.getDate().year(),
                    talk.getDate().month(),
                    talk.getViews().value(),
                    talk.getLikes().value(),
                    talk.getLink().value(),
                    influenceScore,
                    Timestamp.from(Instant.now())
            );
        }
    }

    @Override
    public TedTalk save(TedTalk talk) {
        saveAll(List.of(talk));
        return talk;
    }

    @Override
    public List<TedTalk> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM ted_talks",
                this::mapRowToTedTalk
        );
    }

    @Override
    public Stream<TedTalk> streamAll() {
        return findAll().stream();
    }

    @Override
    public void processBatches(int batchSize, Consumer<List<TedTalk>> processor) {
        // Simple implementation for tests
        processor.accept(findAll());
    }

    @Override
    public Optional<TedTalk> findById(TedTalkId id) {
        try {
            TedTalk talk = jdbcTemplate.queryForObject(
                    "SELECT * FROM ted_talks WHERE id = ?",
                    this::mapRowToTedTalk,
                    id.value()
            );
            return Optional.ofNullable(talk);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<TedTalk> findBySpeakerId(SpeakerId speakerId) {
        return jdbcTemplate.query(
                "SELECT * FROM ted_talks WHERE author = ?",
                this::mapRowToTedTalk,
                speakerId.value().toString()
        );
    }

    @Override
    public List<TedTalk> findByYear(int year) {
        return jdbcTemplate.query(
                "SELECT * FROM ted_talks WHERE talk_year = ?",
                this::mapRowToTedTalk,
                year
        );
    }

    @Override
    public List<TedTalk> findBySpeakerName(String speakerName) {
        return jdbcTemplate.query(
                "SELECT * FROM ted_talks WHERE author = ?",
                this::mapRowToTedTalk,
                speakerName
        );
    }

    @Override
    public void delete(TedTalkId id) {
        jdbcTemplate.update("DELETE FROM ted_talks WHERE id = ?", id.value());
    }

    @Override
    public boolean existsByTitleAndSpeakerId(String title, SpeakerId speakerId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ted_talks WHERE title = ? AND author = ?",
                Integer.class,
                title,
                speakerId.value().toString()
        );
        return count != null && count > 0;
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ted_talks",
                Long.class
        );
        return count != null ? count : 0L;
    }

    private TedTalk mapRowToTedTalk(ResultSet rs, int rowNum) throws SQLException {
        Speaker speaker = Speaker.create(rs.getString("author"), null);

        return TedTalk.create(
                rs.getString("title"),
                speaker,
                new TalkDate(rs.getInt("talk_year"), rs.getInt("talk_month")),
                Views.of(rs.getLong("views")),
                Likes.of(rs.getLong("likes")),
                Link.of(rs.getString("link"))
        );
    }
}

