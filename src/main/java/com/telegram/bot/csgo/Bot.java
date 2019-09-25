package com.telegram.bot.csgo;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.telegram.bot.csgo.model.Constants;

public class Bot extends TelegramLongPollingBot {

	@Override
	public String getBotUsername() {
		return Constants.BOT_NAME;
	}

	@Override
	public String getBotToken() {
		return Constants.BOT_TOKEN;
	}

	@Override
	public void onUpdateReceived(Update update) {
		// We check if the update has a message and the message has text
		if (update.hasMessage() && update.getMessage().hasText()) {
			Long chatId = update.getMessage().getChatId();
			String text = update.getMessage().getText();
			// Help
			if (text.equalsIgnoreCase(".хелп")) {
				sendMessage(chatId, MessageHelper.help());
			}
			// Menu
			if (text.equalsIgnoreCase(".меню")) {
				sendMessage(chatId, MenuHelper.menu());
			}
			// Top Players
			if (text.equalsIgnoreCase(".топ10игроков") || text.equalsIgnoreCase(".топ20игроков")
					|| text.equalsIgnoreCase(".топ30игроков")) {
				Integer count = Integer.parseInt(text.substring(4, 6));
				sendMessage(chatId, MessageHelper.topPlayers(count));
			}
			// Top Teams
			if (text.equalsIgnoreCase(".топ10") || text.equalsIgnoreCase(".топ20") || text.equalsIgnoreCase(".топ30")) {
				Integer count = Integer.parseInt(update.getMessage().getText().substring(4));
				sendMessage(chatId, MessageHelper.topTeams(count));
			}
			// Private message
			if (StringUtils.startsWith(update.getMessage().getText(), "@" + Constants.BOT_NAME)) {
				sendMessage(chatId, MessageHelper.toBot(update.getMessage().getFrom().getUserName()));
			}
		}

		// Check call back from Menu
		if (update.getCallbackQuery() != null) {
			Long chatId = update.getCallbackQuery().getMessage().getChatId();
			deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
			sendMessage(chatId, MenuHelper.checkCallBack(update.getCallbackQuery()));
			
		}
	}

	private void sendMessage(Long chatId, SendMessage msg) {
		msg.setChatId(chatId);
		try {
			execute(msg); // Call method to send the message
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void deleteMessage(Long chatId, Integer msgId) {
		try {
			execute(new DeleteMessage(chatId, msgId));
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

}
