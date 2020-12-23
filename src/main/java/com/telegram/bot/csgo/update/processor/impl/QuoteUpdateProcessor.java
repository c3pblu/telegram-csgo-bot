package com.telegram.bot.csgo.update.processor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.HtmlMessage;
import com.telegram.bot.csgo.model.dao.Sticker;
import com.telegram.bot.csgo.service.HttpService;
import com.telegram.bot.csgo.update.processor.UpdateProcessor;

@Component
public class QuoteUpdateProcessor implements UpdateProcessor {

	private BotController botController;
	private HttpService httpService;
	private Dao dao;

	@Value("${bot.message.uniq.count}")
	private Integer uniqCount;

	private Map<String, List<Sticker>> chatLastStickers = new ConcurrentHashMap<>();

	@Autowired
	public QuoteUpdateProcessor(BotController botController, HttpService httpService, Dao dao) {
		this.botController = botController;
		this.httpService = httpService;
		this.dao = dao;
	}

	private static final String QUOTE_COMMAND = ".цитата";

	@Override
	public void process(Update update) {
		if (update.hasMessage()
				&& (StringUtils.startsWith(update.getMessage().getText(), "@" + botController.getBotUsername())
						|| QUOTE_COMMAND.equalsIgnoreCase(update.getMessage().getText()))) {
			String chatId = getChatId(update);
			botController.send(getSticker(chatId, uniqCount));
			Document doc = httpService
					.getDocument("https://api.forismatic.com/api/1.0/?method=getQuote&format=html&lang=ru");
			botController.send(quote(chatId, doc));
		}
	}

	private SendMessage quote(String chatId, Document doc) {
		StringBuilder text = new StringBuilder();
		text.append(doc.select("cite").text());
		String athor = doc.select("small").text();
		if (!StringUtils.isEmpty(athor)) {
			text.append("\n<b>").append(athor).append("</b>");
		}
		return new HtmlMessage(chatId, String.valueOf(text));
	}

	private SendSticker getSticker(String chatId, Integer uniqCount) {
		List<Sticker> stickers = dao.getStickers();
		int randomSize = stickers.size();
		int randomValue = new Random().nextInt(randomSize);
		Sticker selectedSticker = stickers.get(randomValue);
		List<Sticker> lastStickers = chatLastStickers.get(chatId) != null ? chatLastStickers.get(chatId)
				: new ArrayList<>();
		while (lastStickers.contains(selectedSticker)) {
			randomValue = new Random().nextInt(randomSize);
			selectedSticker = stickers.get(randomValue);
		}

		if (lastStickers.size() < uniqCount) {
			lastStickers.add(selectedSticker);
		} else {
			lastStickers.remove(0);
			lastStickers.add(selectedSticker);
		}
		chatLastStickers.put(chatId, lastStickers);
		return SendSticker.builder().sticker(new InputFile(selectedSticker.getSticker())).chatId(chatId).build();
	}

}
