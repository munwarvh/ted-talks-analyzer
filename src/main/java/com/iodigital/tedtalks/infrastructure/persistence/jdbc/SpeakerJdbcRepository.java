package com.iodigital.tedtalks.infrastructure.persistence.jdbc;

import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.valueobject.SpeakerId;
import com.iodigital.tedtalks.domain.repository.SpeakerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
@Primary
@Slf4j
public class SpeakerJdbcRepository implements SpeakerRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = "INSERT INTO speakers (name, bio) VALUES (?, ?) ON CONFLICT (name) DO UPDATE SET bio = EXCLUDED.bio RETURNING id";
    private static final String SELECT_ALL = "SELECT * FROM speakers";
    private static final String SELECT_BY_NAME = "SELECT * FROM speakers WHERE name = ?";
    private static final String SELECT_BY_NAME_PATTERN = "SELECT * FROM speakers WHERE LOWER(name) LIKE LOWER(?)";
    private static final String EXISTS_BY_NAME = "SELECT COUNT(*) FROM speakers WHERE name = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM speakers";

    public SpeakerJdbcRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Speaker> findAll() {
        log.debug("Finding all speakers");
        return jdbcTemplate.query(SELECT_ALL, this::mapRowToSpeaker);
    }

    @Override
    public Stream<Speaker> streamAll() {
        log.debug("Streaming all speakers");
        return findAll().stream();
    }

    @Override
    public Optional<Speaker> findById(SpeakerId id) {
        log.debug("Finding speaker by id: {}", id);
        try {
            Speaker speaker = jdbcTemplate.queryForObject(SELECT_BY_NAME, this::mapRowToSpeaker, id.value().toString());
            return Optional.ofNullable(speaker);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Speaker> findByName(String name) {
        log.debug("Finding speaker by name: {}", name);
        try {
            Speaker speaker = jdbcTemplate.queryForObject(SELECT_BY_NAME, this::mapRowToSpeaker, name);
            return Optional.ofNullable(speaker);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Speaker> findByNameContaining(String namePattern) {
        log.debug("Finding speakers by name pattern: {}", namePattern);
        return jdbcTemplate.query(SELECT_BY_NAME_PATTERN, this::mapRowToSpeaker, "%" + namePattern + "%");
    }

    @Override
    public Speaker save(Speaker speaker) {
        log.debug("Saving speaker: {}", speaker.getName());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, speaker.getName());
            ps.setString(2, speaker.getBio());
            return ps;
        }, keyHolder);

        return speaker;
    }

    @Override
    public void saveAll(List<Speaker> speakers) {
        log.debug("Saving {} speakers", speakers.size());
        speakers.forEach(this::save);
    }

    @Override
    public void delete(SpeakerId id) {
        log.debug("Deleting speaker with id: {}", id);

        log.warn("Delete by SpeakerId not fully implemented due to ID type mismatch");
    }

    @Override
    public boolean existsByName(String name) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_BY_NAME, Integer.class, name);
        return count != null && count > 0;
    }

    @Override
    public long count() {
        Long count = jdbcTemplate.queryForObject(COUNT_ALL, Long.class);
        return count != null ? count : 0L;
    }

    private Speaker mapRowToSpeaker(ResultSet rs, int rowNum) throws SQLException {
        String name = rs.getString("name");
        String bio = rs.getString("bio");

        // Create speaker from database data
        return Speaker.create(name, bio);
    }
}

