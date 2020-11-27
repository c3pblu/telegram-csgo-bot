package com.telegram.bot.csgo.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegram.bot.csgo.service.BotService;

@Service
public class BotController extends TelegramLongPollingBot {

	@Value(value = "${bot.name}")
	private String botName;
	@Value(value = "${bot.token}")
	private String botToken;

	ObjectFactory<BotService> serviceFactory;

	@Autowired
	public BotController(ObjectFactory<BotService> serviceFactory) {
		this.serviceFactory = serviceFactory;
	}

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
		ExecutorService pool = Executors.newCachedThreadPool();
		pool.execute(serviceFactory.getObject().setUpdate(update));
	}

	public void sendMessage(SendMessage msg) {
		try {
			execute(msg); // Call method to send the message
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String chatId, SendSticker msg) {
		msg.setChatId(chatId);
		try {
			execute(msg); // Call method to send the message
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	public void deleteMessage(String chatId, Integer msgId) {
		try {
			execute(new DeleteMessage(chatId, msgId));
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

}
