package com.iodigital.tedtalks.application.dto;

public record SpeakerInfluenceDto(
        String speaker,
        long totalTalks,
        long totalViews,
        long totalLikes,
        double averageInfluenceScore,
        double totalInfluenceScore,
        int firstTalkYear,
        int lastTalkYear
) {}

