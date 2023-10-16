package com.telegram.bot.csgo.service.http;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import static io.netty.handler.codec.http.HttpResponseStatus.TOO_MANY_REQUESTS;
import static java.time.Duration.ofSeconds;
import static java.util.Optional.ofNullable;
import static reactor.util.retry.Retry.fixedDelay;
import com.telegram.bot.csgo.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebClientHttpService implements HttpService {

    private static final int RETRY_MAX_ATTEMPTS = 5;
    private static final Duration RETRY_DELAY = ofSeconds(60);

    private final WebClient webClient;

    @Override
    public <R> R get(String uri, MultiValueMap<String, String> headers, Function<String, R> responseMapper) {
        return getAsMono(uri, headers, String.class)
                .map(responseMapper)
                .block();
    }

    @Override
    public <R> R get(String uri, MultiValueMap<String, String> headers, Class<R> responseType) {
        return getAsMono(uri, headers, responseType)
                .block();
    }

    @Override
    public <R> R post(String uri, Object body, MultiValueMap<String, String> headers, Class<R> responseType) {
        return webClient.post()
                .uri(uri)
                .bodyValue(body)
                .headers(createHeaders(headers))
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    private <R> Mono<R> getAsMono(String uri, MultiValueMap<String, String> headers, Class<R> responseType) {
        return webClient.get()
                .uri(uri)
                .headers(createHeaders(headers))
                .retrieve()
                .onStatus(onTooManyRequestsStatus(), processTooManyRequests())
                .bodyToMono(responseType)
                .retryWhen(fixedDelay(RETRY_MAX_ATTEMPTS, RETRY_DELAY)
                        .filter(onlyTooManyRequest()));
    }

    private static Consumer<HttpHeaders> createHeaders(MultiValueMap<String, String> headers) {
        return httpHeaders -> ofNullable(headers)
                .ifPresent(h -> httpHeaders.addAll(headers));
    }

    private static Predicate<HttpStatusCode> onTooManyRequestsStatus() {
        return status -> status.value() == TOO_MANY_REQUESTS.code();
    }

    private static Function<ClientResponse, Mono<? extends Throwable>> processTooManyRequests() {
        return response -> {
            log.info("Too Many requests received. Sleeping for {} seconds", RETRY_DELAY.toSeconds());
            return Mono.error(new TooManyRequestsException());
        };
    }

    private static Predicate<? super Throwable> onlyTooManyRequest() {
        return throwable -> throwable instanceof TooManyRequestsException;
    }
}
