package com.iodigital.tedtalks.application.port;

import com.iodigital.tedtalks.application.dto.SpeakerInfluenceDto;
import com.iodigital.tedtalks.application.dto.TedTalkDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TedTalkAnalyzer {

    List<SpeakerInfluenceDto> getTopInfluentialSpeakers(int limit);

    Map<Integer, TedTalkDto> getMostInfluentialTalkPerYear();

    Optional<SpeakerInfluenceDto> analyzeSpeaker(String speakerName);
}

