package com.telegram.bot.csgo;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegram.bot.csgo.helper.MessageHelper;
import com.telegram.bot.csgo.model.BotMessage;
import com.telegram.bot.csgo.model.Constants;
import com.telegram.bot.csgo.model.HelpMessage;
import com.telegram.bot.csgo.model.MenuMessage;

public class Bot extends TelegramLongPollingBot {
	
	private static final String HELP = ".хелп";
	private static final String MENU = ".меню";
	private static final String MATCHES = ".матчи";
	private static final String TOP_10 = ".топ10";
	private static final String TOP_20 = ".топ20";
	private static final String TOP_30 = ".топ30";
	private static final String TOP_10_PLAYERS = ".топ10игроков";
	private static final String TOP_20_PLAYERS = ".топ20игроков";
	private static final String TOP_30_PLAYERS = ".топ30игроков";

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
			// If message was more than 5 minutes before - skip message
			long responseTime = Instant.now().getEpochSecond() - update.getMessage().getDate();
			if (responseTime > 300) {
				return;
			}
			
			Long chatId = update.getMessage().getChatId();
			String text = update.getMessage().getText();
			
			// Help
			if (text.equalsIgnoreCase(HELP)) {
				sendMessage(chatId, new HelpMessage());
			}
			// Menu
			if (text.equalsIgnoreCase(MENU)) {
				sendMessage(chatId, new MenuMessage());
			}
			// Matches
			if (text.equalsIgnoreCase(MATCHES)) {
				sendMessage(chatId, MessageHelper.matches());
			}
			// Top Players
			if (text.equalsIgnoreCase(TOP_10_PLAYERS) || text.equalsIgnoreCase(TOP_20_PLAYERS)
					|| text.equalsIgnoreCase(TOP_30_PLAYERS)) {
				Integer count = Integer.parseInt(text.substring(4, 6));
				sendMessage(chatId, MessageHelper.topPlayers(count));
			}
			// Top Teams
			if (text.equalsIgnoreCase(TOP_10) || text.equalsIgnoreCase(TOP_20) || text.equalsIgnoreCase(TOP_30)) {
				Integer count = Integer.parseInt(update.getMessage().getText().substring(4));
				sendMessage(chatId, MessageHelper.topTeams(count));
			}
			// Private message
			if (StringUtils.startsWith(update.getMessage().getText(), "@" + Constants.BOT_NAME)) {
				sendMessage(chatId, new BotMessage());
			}
		}

		// Check call back from Menu
		if (update.getCallbackQuery() != null) {
			Long chatId = update.getCallbackQuery().getMessage().getChatId();
			deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
			sendMessage(chatId, checkCallBack(update.getCallbackQuery()));
			
		}
	}
	
	
	private SendMessage checkCallBack(CallbackQuery callBack) {
		long responseTime = Instant.now().getEpochSecond() - callBack.getMessage().getDate();
		// If message was more than 5 minutes before - return only message
		if (responseTime > 300) {
			return new SendMessage().setText("Упс, ты слишком долго думал парень!");
		}
		
		String data = callBack.getData();
		
		if (data.equals(Constants.TOP_10)) {
			return MessageHelper.topTeams(10);
		}
		if (data.equals(Constants.TOP_20)) {
			return MessageHelper.topTeams(20);
		}
		if (data.equals(Constants.TOP_30)) {
			return MessageHelper.topTeams(30);
		}
		if (data.equals(Constants.TOP_10_PLAYERS)) {
			return MessageHelper.topPlayers(10);
		}
		if (data.equals(Constants.TOP_20_PLAYERS)) {
			return MessageHelper.topPlayers(20);
		}
		if (data.equals(Constants.TOP_30_PLAYERS)) {
			return MessageHelper.topPlayers(30);
		}
		if (data.equals(Constants.RESULTS)) {
			return new SendMessage().setText("Comming Soon...");
		}
		if (data.equals(Constants.MATCHES)) {
			return MessageHelper.matches();
		}
		return new SendMessage();
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
