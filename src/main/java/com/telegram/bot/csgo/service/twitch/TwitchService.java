package com.telegram.bot.csgo.service.twitch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.service.MessageService;

@Service
public class TwitchService {

	@Value("${twitch.client.id}")
	private String clientId;
	@Value("${twitch.client.secret}")
	private String clientSecret;

	private static final String USER_AGENT_NAME = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
	private static String ACCESS_TOKEN;
	private Map<Long, String> chatPage = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(TwitchService.class);

	private MessageService messageService;

	@Autowired
	public TwitchService(MessageService messageService) {
		this.messageService = messageService;
	}

	public SendMessage getStreams(Long chatid, boolean isNextPage) {
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
		return streams(json);
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

	public SendMessage nextPage() {
		SendMessage sendMessage = new SendMessage();
		InlineKeyboardMarkup markUpInLine = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
		List<InlineKeyboardButton> row = new ArrayList<>();
		row.add(new InlineKeyboardButton().setText("Next 20 Streams »").setCallbackData("nextPage"));
		rowsInLine.add(row);
		markUpInLine.setKeyboard(rowsInLine);
		sendMessage.setReplyMarkup(markUpInLine);
		sendMessage.setText("Go to next Page?");
		return sendMessage;
	}

	public SendMessage streams(JSONObject json) {
		StringBuilder textMessage = new StringBuilder();
		textMessage.append("<b>Live</b>").append(Emoji.EXCL_MARK).append("<b>Streams on Twitch:</b>\n");
		JSONArray arr = json.getJSONArray("data");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject data = arr.getJSONObject(i);
			textMessage.append("<b>(").append(data.getNumber("viewer_count")).append(")</b> ")
					.append("<a href=\'https://www.twitch.tv/").append(data.getString("user_name")).append("\'>")
					.append(data.getString("user_name")).append("</a> ")
					.append(messageService.flagUnicodeFromCountry(data.getString("language").toUpperCase())).append(" ")
					.append(data.getString("title").replace("<", "").replace(">", "")).append("\n");
		}
		LOGGER.debug("Streams final message:\n{}", textMessage);
		return messageService.text(textMessage);

	}

}
