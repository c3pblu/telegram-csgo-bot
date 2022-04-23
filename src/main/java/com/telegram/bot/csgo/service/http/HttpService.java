package com.telegram.bot.csgo.service.http;

import java.util.function.Function;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.util.MultiValueMap;

public interface HttpService {

    default Document getAsDocument(String url) {
        return get(url, null, Jsoup::parse);
    }

    <R> R get(String uri, MultiValueMap<String, String> headers, Function<String, R> mapper);

    <R> R get(String uri, MultiValueMap<String, String> headers, Class<R> responseType);

    <R> R post(String uri, Object body, MultiValueMap<String, String> headers, Class<R> responseType);
}
