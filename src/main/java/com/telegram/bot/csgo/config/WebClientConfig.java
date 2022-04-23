package com.telegram.bot.csgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    private static final int MAX_RESPONSE_BYTES = 10 * 1024 * 1024;

    @Bean
    public ReactorClientHttpConnector clientHttpConnector() {
        return new ReactorClientHttpConnector(
                HttpClient.create()
                        .followRedirect(true)
                        // https://github.com/reactor/reactor-netty/issues/388
                        .keepAlive(false));
    }

    @Bean
    public ExchangeStrategies exchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs()
                        .maxInMemorySize(MAX_RESPONSE_BYTES))
                .build();
    }

    @Bean
    public WebClient webClient(ReactorClientHttpConnector clientHttpConnector, ExchangeStrategies exchangeStrategies) {
        return WebClient.builder()
                .exchangeStrategies(exchangeStrategies)
                .clientConnector(clientHttpConnector)
                .build();
    }
}
