package com.telegram.bot.csgo.service.http;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class HttpService {

	private final static String HLTV = "https://www.hltv.org";
	private final static String USER_AGENT_NAME = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

	public Document getDocument(String url) {
		try {
			return Jsoup.connect(url).userAgent(USER_AGENT_NAME).get();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public JSONObject getJson(String uri, String method, HashMap<String, String> headers) {
		try {
			return new JSONObject(Jsoup.connect(uri).userAgent(USER_AGENT_NAME).method(Method.valueOf(method))
					.headers(headers).ignoreContentType(true).execute().body());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Cacheable("teamProfile")
	public Document getTeamProfile(String url) {
		return getDocument(HLTV + url);
	}

}
