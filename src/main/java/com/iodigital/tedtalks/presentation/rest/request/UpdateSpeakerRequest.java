package com.iodigital.tedtalks.presentation.rest.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating an existing Speaker
 */
public record UpdateSpeakerRequest(
        @NotBlank(message = "Speaker name is required")
        String name,

        String bio  // Optional
) {
}

