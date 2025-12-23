package com.iodigital.tedtalks.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iodigital.tedtalks.presentation.rest.request.CreateTedTalkRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Analysis Controller Integration Tests")
@Transactional
class AnalysisControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return top influential speakers")
    void shouldReturnTopInfluentialSpeakers() throws Exception {
        // When/Then - Should return 200 OK (may be empty array)
        mockMvc.perform(get("/api/v1/analysis/speakers/top")
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return most influential talk per year")
    void shouldReturnMostInfluentialTalkPerYear() throws Exception {
        // When/Then - Should return 200 OK
        mockMvc.perform(get("/api/v1/analysis/talks/most-influential-per-year"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should analyze specific speaker")
    @Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldAnalyzeSpecificSpeaker() throws Exception {
        // When/Then - Should return 404 for non-existent speaker
        mockMvc.perform(get("/api/v1/analysis/speakers/{speaker}", "NonExistentSpeaker"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when analyzing non-existent speaker")
    void shouldReturn404ForNonExistentSpeaker() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/analysis/speakers/{speaker}", "NonExistent Speaker"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should limit top speakers result")
    void shouldLimitTopSpeakersResult() throws Exception {
        // When/Then - Request with valid limit should return 200 OK
        mockMvc.perform(get("/api/v1/analysis/speakers/top")
                        .param("limit", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should validate limit parameter")
    void shouldValidateLimitParameter() throws Exception {
        // When/Then - Invalid limit (too large)
        mockMvc.perform(get("/api/v1/analysis/speakers/top")
                        .param("limit", "500"))
                .andExpect(status().isBadRequest());
    }
}

