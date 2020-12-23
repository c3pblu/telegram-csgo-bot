package com.telegram.bot.csgo.update.processor;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.controller.BotController;

public interface UpdateProcessor {

	void process(Update update);

	default void deleteMenu(BotController botController, Update update) {
		if (update.hasCallbackQuery()) {
			botController.send(new DeleteMessage(String.valueOf(update.getCallbackQuery().getMessage().getChatId()),
					update.getCallbackQuery().getMessage().getMessageId()));
		}
	}

	default String getChatId(Update update) {
		if (update.hasMessage()) {
			return String.valueOf(update.getMessage().getChatId());
		}
		if (update.hasCallbackQuery()) {
			return String.valueOf(update.getCallbackQuery().getMessage().getChatId());
		}
		return null;
	}
}
