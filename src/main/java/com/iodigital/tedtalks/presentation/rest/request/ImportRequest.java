package com.iodigital.tedtalks.presentation.rest.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record ImportRequest(
        @NotNull(message = "File is required")
        MultipartFile file,

        @Min(value = 1, message = "Batch size must be at least 1")
        @Max(value = 10000, message = "Batch size cannot exceed 10000")
        int batchSize
) {
    public ImportRequest {
        if (batchSize <= 0) {
            batchSize = 1000;
        }
    }
}

