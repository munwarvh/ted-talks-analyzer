package com.iodigital.tedtalks.domain.model.valueobject;

public record Likes(long value) {
    public Likes {
        if (value < 0) {
            throw new IllegalArgumentException("Likes cannot be negative");
        }
    }

    public static Likes of(long likes) {
        return new Likes(likes);
    }
}

