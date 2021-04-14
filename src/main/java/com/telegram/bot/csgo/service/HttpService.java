package com.telegram.bot.csgo.service;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
@Slf4j
public class HttpService {

    public static final String HLTV = "https://www.hltv.org";
    private OkHttpClient client = new OkHttpClient();

    public Document getDocument(String url) {
        return Jsoup.parse(getHtml(url, null, "GET"));
    }

    public JSONObject getJson(String uri, String method, Headers headers) {
        return new JSONObject(getHtml(uri, headers, method));
    }

    private String getHtml(String url, Headers headers, String method) {
        if (headers == null) {
            headers = new Headers.Builder().build();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "");
        Request req = new Request.Builder().method(method, "GET".equals(method) ? null : body).headers(headers).url(url)
                .build();
        try (Response res = client.newCall(req).execute()) {
            String responseBody = res.body().string();
            log.debug("Request URL : {}", url);
            log.debug("Response code : {}", res.code());
            log.debug("Response headers : {}", res.headers());
            // Check for "HttpCode 429 - Too Many Requests" header and sleep
            String retryAfter = res.header("Retry-After");
            if (!StringUtils.isBlank(retryAfter)) {
                try {
                    log.debug("Sleeping for {} seconds because of 429 Response Code", retryAfter);
                    Thread.sleep(Integer.parseInt(retryAfter) * 1000);
                    responseBody = getHtml(url, headers, method);
                } catch (NumberFormatException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return responseBody;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
