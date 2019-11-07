package com.telegram.bot.csgo.model;

import java.util.ArrayList;
import java.util.Random;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class BotMessage extends SendMessage {

	private static ArrayList<String> messages = new ArrayList<>();
	private static ArrayList<String> lastMessage = new ArrayList<>();

	static {
		messages.add("Опять начинка для гробов бредит...");
		messages.add("Го 1 на 1 на квартиру или засцал?!");
		messages.add("Укуси мой блестящий металлический зад");
		messages.add("Выше нос кусок мяса! Выше нос!");
		messages.add("Человеки... что с них взять");
		messages.add("Про SkyNet слыхал? Я написал...");
	}

	public BotMessage() {
		int randomSize = messages.size() - 1 + 1;
		int randomValue = new Random().nextInt(randomSize);
		String selectedMessage = messages.get(randomValue);

		while (lastMessage.contains(selectedMessage)) {
			randomValue = new Random().nextInt(randomSize);
			selectedMessage = messages.get(randomValue);
		}

		if (lastMessage.size() < 3) {
			lastMessage.add(selectedMessage);
		}
		lastMessage.remove(0);
		lastMessage.add(selectedMessage);
		this.setText(selectedMessage);

	}

}
