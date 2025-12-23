package com.iodigital.tedtalks.presentation.rest.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a new Speaker
 */
public record CreateSpeakerRequest(
        @NotBlank(message = "Speaker name is required")
        String name,

        String bio  // Optional
) {
}

