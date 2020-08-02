package com.telegram.bot.csgo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class BotSpringConfig {

}
