package com.telegram.bot.csgo.update.processor.impl;

import java.time.LocalDate;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.adaptor.TopPlayersAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.HttpService;
import com.telegram.bot.csgo.update.processor.UpdateProcessor;

@Component
public class TopPlayersUpdateProcessor implements UpdateProcessor {

	private BotController botController;
	private HttpService httpService;
	private TopPlayersAdaptor topPlayersAdaptor;

	@Autowired
	public TopPlayersUpdateProcessor(BotController botController, HttpService httpService,
			TopPlayersAdaptor topPlayersAdaptor) {
		this.botController = botController;
		this.httpService = httpService;
		this.topPlayersAdaptor = topPlayersAdaptor;
	}

	private static final String TOP_10_PLAYERS_COMMAND = ".топ10игроков";
	private static final String TOP_20_PLAYERS_COMMAND = ".топ20игроков";
	private static final String TOP_30_PLAYERS_COMMAND = ".топ30игроков";

	@Override
	public void process(Update update) {
		if (update.hasMessage()) {
			String text = update.getMessage().getText();
			if (TOP_10_PLAYERS_COMMAND.equalsIgnoreCase(text) || TOP_20_PLAYERS_COMMAND.equalsIgnoreCase(text)
					|| TOP_30_PLAYERS_COMMAND.equalsIgnoreCase(text)) {
				Integer count = Integer.parseInt(text.substring(4, 6));
				topPlayers(getChatId(update), count);
			}
		}
		if ((update.hasCallbackQuery())) {
			String data = update.getCallbackQuery().getData();
			if (data != null && data.matches("top[\\d][\\d]players")) {
				topPlayers(getChatId(update), Integer.parseInt(data.replaceAll("\\D", "")));
				deleteMenu(botController, update);
			}
		}
	}

	private void topPlayers(String chatId, int count) {
		String year = String.valueOf(LocalDate.now().getYear());
		Document doc = httpService.getDocument(
				HttpService.HLTV + "/stats/players?startDate=" + year + "-01-01&endDate=" + year + "-12-31");
		botController.send(topPlayersAdaptor.topPlayers(chatId, doc, count));
	}

}
