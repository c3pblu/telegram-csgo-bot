package com.telegram.bot.csgo.config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableMBeanExport
@EnableCaching
public class BotSpringConfig {

    @Bean
    public EntityManager entityManager() {
        final EntityManagerFactory factory = Persistence.createEntityManagerFactory("mysql");
        return factory.createEntityManager();
    }

}
