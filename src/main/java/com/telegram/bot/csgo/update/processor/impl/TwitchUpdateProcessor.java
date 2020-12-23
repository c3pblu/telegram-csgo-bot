package com.telegram.bot.csgo.update.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.TwitchService;
import com.telegram.bot.csgo.update.processor.UpdateProcessor;

@Component
public class TwitchUpdateProcessor implements UpdateProcessor {

	private BotController botController;
	private TwitchService twitchService;

	@Autowired
	public TwitchUpdateProcessor(BotController botController, TwitchService twitchService) {
		this.botController = botController;
		this.twitchService = twitchService;
	}

	private static final String STREAMS_COMMAND = ".стримы";
	private static final String STREAMS_CALLBACK = "streams";
	private static final String NEXT_PAGE_CALLBACK = "nextPage";

	@Override
	public void process(Update update) {
		String chatId = getChatId(update);
		if ((update.hasMessage() && STREAMS_COMMAND.equalsIgnoreCase(update.getMessage().getText()))
				|| (update.hasCallbackQuery() && STREAMS_CALLBACK.equals(update.getCallbackQuery().getData()))) {
			sendMessage(update, chatId, false);
		}
		if (update.hasCallbackQuery() && NEXT_PAGE_CALLBACK.equals(update.getCallbackQuery().getData())) {
			sendMessage(update, chatId, true);
		}

	}

	private void sendMessage(Update update, String chatId, boolean isNextPage) {
		botController.send(twitchService.getStreams(chatId, isNextPage));
		botController.send(twitchService.nextPage(chatId));
		deleteMenu(botController, update);
	}

}
