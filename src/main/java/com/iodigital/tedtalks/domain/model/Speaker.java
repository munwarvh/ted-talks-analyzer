package com.iodigital.tedtalks.domain.model;

import com.iodigital.tedtalks.domain.model.valueobject.SpeakerId;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Speaker {
    private final SpeakerId id;
    private final String name;
    private String bio;
    private final List<TedTalk> talks = new ArrayList<>();

    private Speaker(SpeakerId id, String name, String bio) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        validate();
    }

    private void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Speaker name cannot be empty");
        }
    }

    public static Speaker create(String name, String bio) {
        return new Speaker(SpeakerId.generate(), name, bio);
    }

    public static Speaker withId(SpeakerId id, String name, String bio) {
        return new Speaker(id, name, bio);
    }

    public void addTalk(TedTalk talk) {
        talks.add(talk);
    }

    public void updateBio(String bio) {
        this.bio = bio;
    }

    public double calculateAverageInfluence() {
        if (talks.isEmpty()) return 0.0;

        return talks.stream()
                .mapToDouble(TedTalk::calculateInfluenceScore)
                .average()
                .orElse(0.0);
    }

    public long getTotalViews() {
        return talks.stream()
                .mapToLong(t -> t.getViews().value())
                .sum();
    }

    public long getTotalLikes() {
        return talks.stream()
                .mapToLong(t -> t.getLikes().value())
                .sum();
    }
}