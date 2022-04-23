package com.telegram.bot.csgo.config;

import com.telegram.bot.csgo.config.properties.BotProperties;
import com.telegram.bot.csgo.config.properties.TwitchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties({BotProperties.class, TwitchProperties.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class BotSpringConfig {

}
