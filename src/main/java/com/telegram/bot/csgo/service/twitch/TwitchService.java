package com.telegram.bot.csgo.service.twitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.service.http.HttpService;
import com.telegram.bot.csgo.service.message.MessageService;

import okhttp3.Headers;

@Service
public class TwitchService {

	@Value("${twitch.client.id}")
	private String clientId;
	@Value("${twitch.client.secret}")
	private String clientSecret;

	private static String ACCESS_TOKEN;
	private Map<Long, String> chatPage = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(TwitchService.class);

	private MessageService messageService;
	private HttpService httpService;

	@Autowired
	public TwitchService(MessageService messageService, HttpService httpService) {
		this.messageService = messageService;
		this.httpService = httpService;
	}

	public SendMessage getStreams(Long chatid, boolean isNextPage) {
		if (ACCESS_TOKEN == null || ACCESS_TOKEN.isEmpty()) {
			updateAccessToken();
		} else {
			// Validate token
			Headers headers = new Headers.Builder().add("Authorization", "OAuth " + ACCESS_TOKEN).build();
			JSONObject validateResult = httpService.getJson("https://id.twitch.tv/oauth2/validate", "GET",
					headers);
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
		Headers headers = new Headers.Builder().add("Client-ID", clientId)
				.add("Authorization", "Bearer " + ACCESS_TOKEN).build();
		JSONObject json = httpService.getJson(newUri, "GET", headers);
		String nextPage = json.getJSONObject("pagination").getString("cursor");
		LOGGER.debug("NextPage ID: {}", nextPage);
		chatPage.put(chatid, nextPage);
		return streams(json);
	}

	private void updateAccessToken() {
		JSONObject accessToken = httpService.getJson("https://id.twitch.tv/oauth2/token?client_id=" + clientId
				+ "&client_secret=" + clientSecret + "&grant_type=client_credentials", "POST", null);
		if (accessToken != null) {
			ACCESS_TOKEN = accessToken.getString("access_token");
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
		return messageService.htmlMessage(textMessage);

	}

}
