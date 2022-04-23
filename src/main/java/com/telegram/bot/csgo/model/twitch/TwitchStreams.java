package com.telegram.bot.csgo.model.twitch;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwitchStreams {

    private Pagination pagination;
    private List<Stream> data;

}
