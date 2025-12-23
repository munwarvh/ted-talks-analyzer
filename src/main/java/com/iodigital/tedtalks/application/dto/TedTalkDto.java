package com.iodigital.tedtalks.application.dto;

import com.iodigital.tedtalks.domain.model.Speaker;
import com.iodigital.tedtalks.domain.model.TedTalk;

public record TedTalkDto(
        String id,
        String title,
        Speaker speaker,
        int year,
        int month,
        long views,
        long likes,
        String link,
        double influenceScore
) {
    public static TedTalkDto fromDomain(TedTalk talk) {
        return new TedTalkDto(
                talk.getId().value().toString(),
                talk.getTitle(),
                talk.getSpeaker(),
                talk.getDate().year(),
                talk.getDate().month(),
                talk.getViews().value(),
                talk.getLikes().value(),
                talk.getLink().value(),
                talk.calculateInfluenceScore()
        );
    }
}

