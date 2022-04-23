package com.telegram.bot.csgo.model.twitch;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Stream {

    private List<String> tagIds;
    private String userName;
    private String language;
    private Boolean isMature;
    private String type;
    private String title;
    private String thumbnailUrl;
    private String gameName;
    private String userId;
    private String userLogin;
    private String startedAt;
    private String id;
    private int viewerCount;
    private String gameId;
}
