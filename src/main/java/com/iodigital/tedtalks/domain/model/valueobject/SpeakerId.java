package com.iodigital.tedtalks.domain.model.valueobject;

import java.util.UUID;

public record SpeakerId(UUID value) {

    public SpeakerId {
        if (value == null) {
            throw new IllegalArgumentException("Speaker ID cannot be null");
        }
    }

    public static SpeakerId generate() {
        return new SpeakerId(UUID.randomUUID());
    }

    public static SpeakerId fromString(String id) {
        try {
            return new SpeakerId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Speaker ID format: " + id, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}