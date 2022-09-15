package com.telegram.bot.csgo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("bot")
@Getter
@Setter
public final class BotProperties {

    private String name;
    private String token;
    private Long callbackTimeout;
    private Message message;
    private Scheduler scheduler;

    @Getter
    @Setter
    public static class Message {
        private Long timeout;
        private Integer uniqCount;
    }

    @Getter
    @Setter
    public static class Scheduler {
        private String chatId;
    }

}
