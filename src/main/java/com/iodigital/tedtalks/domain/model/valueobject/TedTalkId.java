package com.iodigital.tedtalks.domain.model.valueobject;

import java.util.UUID;

public record TedTalkId(UUID value) {

    public TedTalkId {
        if (value == null) {
            throw new IllegalArgumentException("TedTalk ID cannot be null");
        }
    }

    public static TedTalkId generate() {
        return new TedTalkId(UUID.randomUUID());
    }

    public static TedTalkId fromString(String id) {
        try {
            return new TedTalkId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid TedTalk ID format: " + id, e);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}