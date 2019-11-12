package com.telegram.bot.csgo;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
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

@Component
public class Bot extends TelegramLongPollingBot {

	@Value(value = "${bot.name}")
	private String botName;
	@Value(value = "${bot.token}")
	private String botToken;
	@Value(value = "${bot.scheduler.chat.id}")
	private Long schedulerChatId;
	@Value(value = "${bot.message.timeout}")
	private Long messageTimeout;

	@Override
	public String getBotUsername() {
		return botName;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
	public void onUpdateReceived(Update update) {
		// We check if the update has a message and the message has text
		if (update.hasMessage() && update.getMessage().hasText()) {
			// If message timeout - skip message
			if (isTimeout(update)) {
				return;
			}

			Long chatId = update.getMessage().getChatId();
			String text = update.getMessage().getText();

			// Help
			if (text.equalsIgnoreCase(Constants.HELP)) {
				sendMessage(chatId, new HelpMessage());
			}
			// Menu
			if (text.equalsIgnoreCase(Constants.MENU)) {
				sendMessage(chatId, new MenuMessage());
			}
			// Matches
			if (text.equalsIgnoreCase(Constants.MATCHES)) {
				sendMessage(chatId, MessageHelper.matches());
			}
			// Results
			if (text.equalsIgnoreCase(Constants.RESULTS)) {
				sendMessage(chatId, MessageHelper.results());
			}
			// Top Players
			if (text.equalsIgnoreCase(Constants.TOP_10_PLAYERS) || text.equalsIgnoreCase(Constants.TOP_20_PLAYERS)
					|| text.equalsIgnoreCase(Constants.TOP_30_PLAYERS)) {
				Integer count = Integer.parseInt(text.substring(4, 6));
				sendMessage(chatId, MessageHelper.topPlayers(count));
			}
			// Top Teams
			if (text.equalsIgnoreCase(Constants.TOP_10) || text.equalsIgnoreCase(Constants.TOP_20)
					|| text.equalsIgnoreCase(Constants.TOP_30)) {
				Integer count = Integer.parseInt(update.getMessage().getText().substring(4));
				sendMessage(chatId, MessageHelper.topTeams(count));
			}
			// Private message
			if (StringUtils.startsWith(update.getMessage().getText(), "@" + botName)) {
				sendMessage(chatId, new BotMessage());
			}
		}

		// Check call back from Menu
		if (update.getCallbackQuery() != null) {
			Long chatId = update.getCallbackQuery().getMessage().getChatId();
			sendMessage(chatId, checkCallBack(update.getCallbackQuery()));
			deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());

		}
	}

	private SendMessage checkCallBack(CallbackQuery callBack) {
		// If message timeout - return only message
		if (isTimeout(callBack)) {
			return new SendMessage().setText(Constants.OOPS);
		}

		String data = callBack.getData();

		if (data.equals(Constants.DATA_TOP_10)) {
			return MessageHelper.topTeams(10);
		}
		if (data.equals(Constants.DATA_TOP_20)) {
			return MessageHelper.topTeams(20);
		}
		if (data.equals(Constants.DATA_TOP_30)) {
			return MessageHelper.topTeams(30);
		}
		if (data.equals(Constants.DATA_TOP_10_PLAYERS)) {
			return MessageHelper.topPlayers(10);
		}
		if (data.equals(Constants.DATA_TOP_20_PLAYERS)) {
			return MessageHelper.topPlayers(20);
		}
		if (data.equals(Constants.DATA_TOP_30_PLAYERS)) {
			return MessageHelper.topPlayers(30);
		}
		if (data.equals(Constants.DATA_MATCHES)) {
			return MessageHelper.matches();
		}
		if (data.equals(Constants.DATA_RESULTS)) {
			return MessageHelper.results();
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

	private boolean isTimeout(Object obj) {
		Update update = null;
		CallbackQuery callBack = null;

		if (obj instanceof Update) {
			update = (Update) obj;
		}

		if (obj instanceof CallbackQuery) {
			callBack = (CallbackQuery) obj;
		}

		if (update != null) {
			long responseTime = Instant.now().getEpochSecond() - update.getMessage().getDate();
			if (responseTime > messageTimeout) {
				return true;
			}
		}

		if (callBack != null) {
			long responseTime = Instant.now().getEpochSecond() - callBack.getMessage().getDate();
			if (responseTime > messageTimeout) {
				return true;
			}
		}
		return false;
	}

	@Scheduled(cron = "${bot.scheduler.matches.cron}")
	private void todayMatchesScheduler() {
		sendMessage(schedulerChatId, MessageHelper.matchesForToday());
		sendMessage(schedulerChatId, MessageHelper.matches());
	}

	@Scheduled(cron = "${bot.scheduler.results.cron}")
	private void todayResultsScheduler() {
		sendMessage(schedulerChatId, MessageHelper.resultsForToday());
		sendMessage(schedulerChatId, MessageHelper.results());
	}

}
