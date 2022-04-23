package com.telegram.bot.csgo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("twitch")
@Getter
@Setter
public final class TwitchProperties {

    private String clientId;
    private String clientSecret;
}
