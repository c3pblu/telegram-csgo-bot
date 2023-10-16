package com.telegram.bot.csgo;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration;

@SpringBootApplication
@Import(TelegramBotStarterConfiguration.class)
public class BotApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(BotApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
