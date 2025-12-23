package com.iodigital.tedtalks.domain.model.valueobject;

import java.util.regex.Pattern;

public record Link(String value) {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://[^\\s/$.?#].[^\\s]*$", Pattern.CASE_INSENSITIVE);

    public Link {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Link cannot be null or empty");
        }

        if (!URL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid URL format: " + value);
        }
    }

    public static Link of(String link) {
        return new Link(link);
    }
}

