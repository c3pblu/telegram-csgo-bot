package com.telegram.bot.csgo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class TwitchService {
	
	@Value("${twitch.client.id}")
	private String clientId;
	
	private Map<Long, String> chatPage = new HashMap<>();

	private static final String TWITCH = "https://api.twitch.tv/helix/streams?game_id=32399&language=en&language=ru";
	private static final String USER_AGENT_NAME = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
	private static final Logger LOGGER = LoggerFactory.getLogger(TwitchService.class);

	public JSONObject getStreams(Long chatid, boolean isNextPage) {
		String newUri = TWITCH;
		if (isNextPage) {
			String currentPage = chatPage.get(chatid);
			newUri = newUri.concat("&after=" + currentPage);
			LOGGER.debug("Current page ID: {}", currentPage);
		}
		JSONObject json = getJson(newUri);
		String nextPage = json.getJSONObject("pagination").getString("cursor");
		LOGGER.debug("NextPage ID: {}", nextPage);
		chatPage.put(chatid, nextPage);
		return json;
	}

	public JSONObject getJson(String uri) {
		try {
			URL url = new URL(uri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT_NAME);
			con.setRequestProperty("Client-ID", clientId);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			con.disconnect();
			return new JSONObject(content.toString());

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
