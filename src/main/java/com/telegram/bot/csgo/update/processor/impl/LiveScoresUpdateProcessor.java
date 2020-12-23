package com.telegram.bot.csgo.update.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.model.HtmlMessage;
import com.telegram.bot.csgo.service.LiveScoresService;
import com.telegram.bot.csgo.update.processor.UpdateProcessor;

@Component
public class LiveScoresUpdateProcessor implements UpdateProcessor {

	private BotController botController;
	private LiveScoresService liveScoresService;;

	@Autowired
	public LiveScoresUpdateProcessor(BotController botController, LiveScoresService liveScoresService) {
		this.botController = botController;
		this.liveScoresService = liveScoresService;
	}

	private static final String SCOREBOT_HELP_COMMAND = ".трансляции";
	private static final String STOP_COMMAND = ".стоп";
	private static final String SCOREBOT_HELP_CALLBACK = "scorebot";
	private static final String START_COMMAND = "\\.старт\\-\\d*";

	@Override
	public void process(Update update) {
		String chatId = getChatId(update);
		if (update.hasMessage()) {
			String text = update.getMessage().getText();
			if (SCOREBOT_HELP_COMMAND.equalsIgnoreCase(text)) {
				botController.send(scorebotHelpMessage(chatId));
			} else if (text.matches(START_COMMAND)) {
				liveScoresService.start(chatId, text.substring(7));
			} else if (STOP_COMMAND.equalsIgnoreCase(text)) {
				liveScoresService.stop(chatId, true);
			}
		}
		if (update.hasCallbackQuery() && SCOREBOT_HELP_CALLBACK.equals(update.getCallbackQuery().getData())) {
			botController.send(scorebotHelpMessage(chatId));
			deleteMenu(botController, update);
		}

	}

	private SendMessage scorebotHelpMessage(String chatId) {
		String text = Emoji.INFO
				+ " Для запуска трансляции:\n.<b>старт-1234567</b> (где 1234567 это Match ID - его можно посмотреть в .мачти)\n<b>.стоп</b> - остановить трансяцию";
		return new HtmlMessage(chatId, text);
	}

}
