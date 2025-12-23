package com.iodigital.tedtalks.presentation.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request DTO for creating a new TED Talk
 */
public record CreateTedTalkRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Speaker name is required")
        String speaker,

        @NotBlank(message = "Date is required (format: 'December 2021')")
        String date,

        @NotNull(message = "Views count is required")
        @PositiveOrZero(message = "Views must be zero or positive")
        Long views,

        @NotNull(message = "Likes count is required")
        @PositiveOrZero(message = "Likes must be zero or positive")
        Long likes,

        @NotBlank(message = "Link is required")
        String link
) {
}

