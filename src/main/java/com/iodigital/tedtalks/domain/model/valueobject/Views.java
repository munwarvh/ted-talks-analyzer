package com.iodigital.tedtalks.domain.model.valueobject;

public record Views(long value) {
    public Views {
        if (value < 0) {
            throw new IllegalArgumentException("Views cannot be negative");
        }
    }

    public static Views of(long views) {
        return new Views(views);
    }

    public Views add(Views other) {
        return new Views(this.value + other.value);
    }
}

