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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TedTalk Controller Integration Tests")
@Transactional
class TedTalkControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return all TED talks")
    void shouldReturnAllTedTalks() throws Exception {
        // When/Then - Should return empty array initially
        mockMvc.perform(get("/api/v1/tedtalks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }


    @Test
    @DisplayName("Should return 400 when creating talk with invalid data")
    void shouldReturn400WhenCreatingInvalidTalk() throws Exception {
        // Given - Missing required field
        String invalidRequest = "{\"title\": \"\", \"speaker\": \"Test\"}";

        // When/Then
        mockMvc.perform(post("/api/v1/tedtalks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return talks by year")
    void shouldReturnTalksByYear() throws Exception {
        // When/Then - Should return empty array for year with no talks
        mockMvc.perform(get("/api/v1/tedtalks/year/2020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return talks count")
    void shouldReturnTalksCount() throws Exception {
        // When/Then - Should return 0 or positive number
        mockMvc.perform(get("/api/v1/tedtalks/count"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesRegex("\\d+")));
    }

    @Test
    @DisplayName("Should search talks by title")
    void shouldSearchTalksByTitle() throws Exception {
        // When/Then - Should return array (empty or with results)
        mockMvc.perform(get("/api/v1/tedtalks/search")
                        .param("title", "Climate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

