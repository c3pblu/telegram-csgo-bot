package com.telegram.bot.csgo.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwitchService {

	@Value("${twitch.client.id}")
	private String clientId;
	@Value("${twitch.client.secret}")
	private String clientSecret;

	private Map<Long, String> chatPage = new HashMap<>();

	private static final String USER_AGENT_NAME = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
	private static String ACCESS_TOKEN;
	private static final Logger LOGGER = LoggerFactory.getLogger(TwitchService.class);

	public JSONObject getStreams(Long chatid, boolean isNextPage) {
		if (ACCESS_TOKEN == null || ACCESS_TOKEN.isEmpty()) {
			updateAccessToken();
		} else {
			// Validate token
			HashMap<String, String> headers = new HashMap<>();
			headers.put("Authorization", "OAuth " + ACCESS_TOKEN);
			JSONObject validateResult = httpRequest("https://id.twitch.tv/oauth2/validate", "GET", headers);
			// If not valid get new token
			if (validateResult == null) {
				updateAccessToken();
			}
		}

		String newUri = "https://api.twitch.tv/helix/streams?game_id=32399&language=en&language=ru";
		if (isNextPage) {
			String currentPage = chatPage.get(chatid);
			newUri = newUri.concat("&after=" + currentPage);
			LOGGER.debug("Current page ID: {}", currentPage);
		}
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Client-ID", clientId);
		headers.put("Authorization", "Bearer " + ACCESS_TOKEN);
		JSONObject json = httpRequest(newUri, "GET", headers);
		String nextPage = json.getJSONObject("pagination").getString("cursor");
		LOGGER.debug("NextPage ID: {}", nextPage);
		chatPage.put(chatid, nextPage);
		return json;
	}

	private void updateAccessToken() {
		JSONObject accessToken = httpRequest("https://id.twitch.tv/oauth2/token?client_id=" + clientId
				+ "&client_secret=" + clientSecret + "&grant_type=client_credentials", "POST", new HashMap<>());
		if (accessToken != null) {
			ACCESS_TOKEN = accessToken.getString("access_token");
		}
	}

	public JSONObject httpRequest(String uri, String method, HashMap<String, String> headers) {
		try {
			return new JSONObject(Jsoup.connect(uri).userAgent(USER_AGENT_NAME).method(Method.valueOf(method))
					.headers(headers).ignoreContentType(true).execute().body());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

}
