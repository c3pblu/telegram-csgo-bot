package com.telegram.bot.csgo;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegram.bot.csgo.model.Constants;
import com.telegram.bot.csgo.model.Help;
import com.telegram.bot.csgo.model.Top10;

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
			checkMessage(update, "/top10", new Top10());
//	        GetMethod get = new GetMethod("http://httpcomponents.apache.org");
//	        // execute method and handle any error responses.
//	        ...
//	        InputStream in = get.getResponseBodyAsStream();
//	        // Process the data from the input stream.
//	        get.releaseConnection();
			checkMessage(update, "/top100", new Top10());
			checkMessage(update, "/top100players", new Top10());
			checkMessage(update, "/help", new Help());
		}

	}

	private void checkMessage(Update update, String checkMsg, SendMessage outputMsg) {
		if (update.getMessage().getText().equals(checkMsg)) {
			outputMsg.setChatId(update.getMessage().getChatId());
			try {
				execute(outputMsg); // Call method to send the message
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}

	}

}
