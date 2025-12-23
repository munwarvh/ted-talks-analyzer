package com.iodigital.tedtalks.domain.model;

import com.iodigital.tedtalks.domain.model.valueobject.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TedTalk Domain Model Tests")
class TedTalkTest {

    @Test
    @DisplayName("Should calculate influence score correctly with views and likes")
    void shouldCalculateInfluenceScoreCorrectly() {
        // Given
        Speaker speaker = Speaker.create("Test Speaker", null);
        TalkDate date = TalkDate.fromString("January 2020");
        Views views = Views.of(1000000L);
        Likes likes = Likes.of(50000L);
        Link link = Link.of("https://ted.com/talks/test");

        // When
        TedTalk talk = TedTalk.create(
                "Test Talk",
                speaker,
                date,
                views,
                likes,
                link
        );

        // Then
        double expectedScore = (1000000 * 0.7) + (50000 * 0.3);
        assertThat(talk.calculateInfluenceScore()).isEqualTo(expectedScore);
    }

    @Test
    @DisplayName("Should calculate influence score with zero likes")
    void shouldCalculateInfluenceScoreWithZeroLikes() {
        // Given
        Speaker speaker = Speaker.create("Test Speaker", null);
        TalkDate date = TalkDate.fromString("January 2020");
        Views views = Views.of(500000L);
        Likes likes = Likes.of(0L);
        Link link = Link.of("https://ted.com/talks/test");

        // When
        TedTalk talk = TedTalk.create("Test Talk", speaker, date, views, likes, link);

        // Then
        assertThat(talk.calculateInfluenceScore()).isEqualTo(500000.0 * 0.7);
    }

    @Test
    @DisplayName("Should calculate influence score with high engagement")
    void shouldCalculateInfluenceScoreWithHighEngagement() {
        // Given
        Speaker speaker = Speaker.create("Hans Rosling", null);
        TalkDate date = TalkDate.fromString("February 2006");
        Views views = Views.of(15000000L);
        Likes likes = Likes.of(400000L);
        Link link = Link.of("https://ted.com/talks/hans_rosling");

        // When
        TedTalk talk = TedTalk.create(
                "The best stats you've ever seen",
                speaker,
                date,
                views,
                likes,
                link
        );

        // Then
        double expectedScore = (15000000 * 0.7) + (400000 * 0.3);
        assertThat(talk.calculateInfluenceScore()).isEqualTo(expectedScore);
        assertThat(talk.getTitle()).isEqualTo("The best stats you've ever seen");
        assertThat(talk.getSpeaker().getName()).isEqualTo("Hans Rosling");
    }

    @Test
    @DisplayName("Should create TedTalk with all required fields")
    void shouldCreateTedTalkWithAllFields() {
        // Given
        Speaker speaker = Speaker.create("Test Speaker", "Bio");
        TalkDate date = TalkDate.fromString("March 2021");
        Views views = Views.of(100000L);
        Likes likes = Likes.of(5000L);
        Link link = Link.of("https://ted.com/talks/test-talk");

        // When
        TedTalk talk = TedTalk.create(
                "Test Talk Title",
                speaker,
                date,
                views,
                likes,
                link
        );

        // Then
        assertThat(talk).isNotNull();
        assertThat(talk.getTitle()).isEqualTo("Test Talk Title");
        assertThat(talk.getSpeaker().getName()).isEqualTo("Test Speaker");
        assertThat(talk.getDate().year()).isEqualTo(2021);
        assertThat(talk.getDate().month()).isEqualTo(3);
        assertThat(talk.getViews().value()).isEqualTo(100000L);
        assertThat(talk.getLikes().value()).isEqualTo(5000L);
        assertThat(talk.getLink().value()).isEqualTo("https://ted.com/talks/test-talk");
    }
}

