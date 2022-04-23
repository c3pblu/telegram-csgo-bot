package com.telegram.bot.csgo.model.twitch;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Token {

    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
}
