package com.iodigital.tedtalks.infrastructure.persistence.jdbc;

import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;
import com.iodigital.tedtalks.domain.model.valueobject.*;
import com.iodigital.tedtalks.domain.repository.TedTalkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Repository
@Primary
@Slf4j
public class TedTalkJdbcRepository implements TedTalkRepository {

    private static final String INSERT_SQL = """
        INSERT INTO ted_talks (
            title, author, date, talk_year, talk_month, 
            views, likes, link, created_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (title, author) 
        DO UPDATE SET 
            views = EXCLUDED.views,
            likes = EXCLUDED.likes,
            updated_at = CURRENT_TIMESTAMP
        """;

    private static final String SELECT_BY_SPEAKER = """
        SELECT * FROM ted_talks WHERE author = ? ORDER BY influence_score DESC
        """;

    private final JdbcTemplate simpleJdbcTemplate;

    public TedTalkJdbcRepository(DataSource dataSource) {
        this.simpleJdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void saveAll(List<TedTalk> talks) {
        if (talks.isEmpty()) {
            return;
        }

        int[] updateCounts = simpleJdbcTemplate.batchUpdate(
                INSERT_SQL,
                new BatchPreparedStatementSetter() {
                    final Iterator<TedTalk> iterator = talks.iterator();

                    @Override
                    public void setValues(PreparedStatement ps, int i)
                            throws SQLException {
                        TedTalk talk = iterator.next();

                        ps.setString(1, talk.getTitle());
                        ps.setString(2, talk.getSpeaker().getName());
                        ps.setDate(3, java.sql.Date.valueOf(talk.getDate().toLocalDate()));
                        ps.setInt(4, talk.getDate().year());
                        ps.setInt(5, talk.getDate().month());
                        ps.setLong(6, talk.getViews().value());
                        ps.setLong(7, talk.getLikes().value());
                        ps.setString(8, talk.getLink().value());
                        ps.setTimestamp(9, Timestamp.from(Instant.now()));
                    }

                    @Override
                    public int getBatchSize() {
                        return talks.size();
                    }
                }
        );

        log.debug("Batch insert completed. Update counts: {}",
                Arrays.toString(updateCounts));
    }

    @Override
    public List<TedTalk> findBySpeakerName(String speaker) {
        return simpleJdbcTemplate.query(
                SELECT_BY_SPEAKER,
                new Object[]{speaker},
                this::mapRowToTedTalk
        );
    }

    @Override
    public List<TedTalk> findByYear(int year) {
        String sql = "SELECT * FROM ted_talks WHERE talk_year = ?";
        return simpleJdbcTemplate.query(
                sql,
                new Object[]{year},
                this::mapRowToTedTalk
        );
    }

    @Override
    public List<TedTalk> findAll() {
        return simpleJdbcTemplate.query(
                "SELECT * FROM ted_talks",
                this::mapRowToTedTalk
        );
    }

    @Override
    public Stream<TedTalk> streamAll() {
        log.info("Streaming all TED talks from database");
        return findAll().stream();
    }

    @Override
    public void processBatches(int batchSize, Consumer<List<TedTalk>> processor) {
        log.info("Processing TED talks in batches of {}", batchSize);
        String sql = "SELECT * FROM ted_talks LIMIT ? OFFSET ?";

        long totalCount = count();
        int totalPages = (int) Math.ceil((double) totalCount / batchSize);

        for (int page = 0; page < totalPages; page++) {
            int offset = page * batchSize;
            List<TedTalk> batch = simpleJdbcTemplate.query(
                    sql,
                    new Object[]{batchSize, offset},
                    this::mapRowToTedTalk
            );

            processor.accept(batch);
            log.debug("Processed batch {}/{}", page + 1, totalPages);
        }
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

    @Override
    public TedTalk save(TedTalk talk) {
        saveAll(List.of(talk));
        return talk;
    }

    @Override
    public Optional<TedTalk> findById(TedTalkId id) {
        String sql = "SELECT * FROM ted_talks WHERE id = ?";
        try {
            TedTalk talk = simpleJdbcTemplate.queryForObject(
                    sql,
                    new Object[]{id.value()},
                    this::mapRowToTedTalk
            );
            return Optional.ofNullable(talk);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(TedTalkId id) {
        simpleJdbcTemplate.update("DELETE FROM ted_talks WHERE id = ?", id.value());
    }

    @Override
    public boolean existsByTitleAndSpeakerId(String title, SpeakerId speakerId) {
        String sql = "SELECT COUNT(*) FROM ted_talks WHERE title = ? AND author = ?";
        Integer count = simpleJdbcTemplate.queryForObject(
                sql,
                new Object[]{title, speakerId.value().toString()},
                Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public List<TedTalk> findBySpeakerId(SpeakerId speakerId) {
        String sql = "SELECT * FROM ted_talks WHERE author = ? ORDER BY influence_score DESC";
        return simpleJdbcTemplate.query(
                sql,
                new Object[]{speakerId.value().toString()},
                this::mapRowToTedTalk
        );
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM ted_talks";
        Long count = simpleJdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}

