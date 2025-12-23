package com.iodigital.tedtalks.application.dto;

import com.iodigital.tedtalks.domain.model.Speaker;

public record SpeakerDto(
        String id,
        String name,
        String bio,
        long totalTalks,
        long totalViews,
        long totalLikes
) {
    public static SpeakerDto fromDomain(Speaker speaker, long totalTalks, long totalViews, long totalLikes) {
        return new SpeakerDto(
                speaker.getId().value().toString(),
                speaker.getName(),
                speaker.getBio(),
                totalTalks,
                totalViews,
                totalLikes
        );
    }

    public static SpeakerDto fromDomainBasic(Speaker speaker) {
        return new SpeakerDto(
                speaker.getId().value().toString(),
                speaker.getName(),
                speaker.getBio(),
                0,
                0,
                0
        );
    }
}

