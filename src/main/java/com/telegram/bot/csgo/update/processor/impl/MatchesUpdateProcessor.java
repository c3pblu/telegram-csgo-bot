package com.telegram.bot.csgo.update.processor.impl;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.adaptor.MatchesAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.HttpService;
import com.telegram.bot.csgo.update.processor.UpdateProcessor;

@Component
public class MatchesUpdateProcessor implements UpdateProcessor {

	private BotController botController;
	private HttpService httpService;
	private MatchesAdaptor matchesAdaptor;

	@Autowired
	public MatchesUpdateProcessor(BotController botController, HttpService httpService, MatchesAdaptor matchesAdaptor) {
		this.botController = botController;
		this.httpService = httpService;
		this.matchesAdaptor = matchesAdaptor;
	}

	private static final String MATCHES_COMMAND = ".матчи";
	private static final String MATCHES_CALLBACK = "matches";

	@Override
	public void process(Update update) {
		if ((update.hasMessage() && MATCHES_COMMAND.equalsIgnoreCase(update.getMessage().getText()))
				|| (update.hasCallbackQuery() && MATCHES_CALLBACK.equals(update.getCallbackQuery().getData()))) {
			Document doc = httpService.getDocument(HttpService.HLTV + "/matches");
			botController.send(matchesAdaptor.matches(getChatId(update), doc));
			deleteMenu(botController, update);
		}
	}
}
