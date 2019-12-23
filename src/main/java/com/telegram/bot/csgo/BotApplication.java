package com.telegram.bot.csgo;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class BotApplication {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        new SpringApplicationBuilder(BotApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}