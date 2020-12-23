package com.telegram.bot.csgo.update.processor.impl;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.adaptor.ResultsAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.HttpService;
import com.telegram.bot.csgo.update.processor.UpdateProcessor;

@Component
public class ResultsUpdateProcessor implements UpdateProcessor {

	private BotController botController;
	private HttpService httpService;
	private ResultsAdaptor resultsAdaptor;

	@Autowired
	public ResultsUpdateProcessor(BotController botController, HttpService httpService, ResultsAdaptor resultsAdaptor) {
		this.botController = botController;
		this.httpService = httpService;
		this.resultsAdaptor = resultsAdaptor;
	}

	private static final String RESULTS_COMMAND = ".результаты";
	private static final String RESULTS_CALLBACK = "results";

	@Override
	public void process(Update update) {
		if ((update.hasMessage() && RESULTS_COMMAND.equalsIgnoreCase(update.getMessage().getText()))
				|| (update.hasCallbackQuery() && RESULTS_CALLBACK.equals(update.getCallbackQuery().getData()))) {
			Document doc = httpService.getDocument(HttpService.HLTV + "/results");
			botController.send(resultsAdaptor.results(getChatId(update), doc));
			deleteMenu(botController, update);
		}
	}
}
