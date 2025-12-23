package com.iodigital.tedtalks.domain.model;

import com.iodigital.tedtalks.domain.model.valueobject.Link;
import com.iodigital.tedtalks.domain.model.valueobject.Likes;
import com.iodigital.tedtalks.domain.model.valueobject.TalkDate;
import com.iodigital.tedtalks.domain.model.valueobject.TedTalkId;
import com.iodigital.tedtalks.domain.model.valueobject.Views;
import lombok.Getter;

@Getter
public class TedTalk {
    private final TedTalkId id;
    private final String title;
    private final Speaker speaker;
    private final TalkDate date;
    private final Views views;
    private final Likes likes;
    private final Link link;

    private TedTalk(TedTalkId id, String title, Speaker speaker, TalkDate date,
                    Views views, Likes likes, Link link) {
        this.id = id;
        this.title = title;
        this.speaker = speaker;
        this.date = date;
        this.views = views;
        this.likes = likes;
        this.link = link;
        validate();
    }

    private void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (speaker == null) {
            throw new IllegalArgumentException("Speaker is required");
        }
    }

    public static TedTalk create(String title, Speaker speaker, TalkDate date,
                                 Views views, Likes likes, Link link) {
        return new TedTalk(
                TedTalkId.generate(),
                title,
                speaker,
                date,
                views,
                likes,
                link
        );
    }

    public static TedTalk withId(TedTalkId id, String title, Speaker speaker, TalkDate date,
                                 Views views, Likes likes, Link link) {
        return new TedTalk(id, title, speaker, date, views, likes, link);
    }

    public double calculateInfluenceScore() {
        return (views.value() * 0.7) + (likes.value() * 0.3);
    }
}