package com.telegram.bot.csgo.service.http;

import java.io.IOException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class HttpService {

	private final static String HLTV = "https://www.hltv.org";

	public Document getDocument(String url) {
		return Jsoup.parse(getHtml(url, null, "GET"));
	}

	public JSONObject getJson(String uri, String method, Headers headers) {
		return new JSONObject(getHtml(uri, headers, method));
	}

	@Cacheable("teamProfile")
	public Document getTeamProfile(String url) {
		return getDocument(HLTV + url);
	}

	private String getHtml(String url, Headers headers, String method) {
		if (headers == null) {
			headers = new Headers.Builder().build();
		}
		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), "");
		Request req = new Request.Builder().method(method, "GET".equals(method) ? null : body).headers(headers).url(url)
				.build();
		try (Response res = client.newCall(req).execute()) {
			return res.body().string();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

}
