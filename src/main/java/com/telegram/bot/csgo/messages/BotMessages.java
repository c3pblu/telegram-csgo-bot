package com.telegram.bot.csgo.messages;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class BotMessages {

	private ArrayList<String> messages = new ArrayList<>();
	private ArrayList<String> lastMessage = new ArrayList<>();
	@Value(value = "${bot.message.uniq.count}")
	private Integer uniqCount;

	@PostConstruct
	private void addMessages() {
		messages.add("Опять начинка для гробов бредит...");
		messages.add("Го 1 на 1 на квартиру или засцал?!");
		messages.add("Укуси мой блестящий металлический зад");
		messages.add("Выше нос кусок мяса! Выше нос!");
		messages.add("Человеки... что с них взять");
		messages.add("Про SkyNet слыхал? Я написал...");
	}

	public SendMessage sendBotMessage() {
		int randomSize = messages.size() - 1 + 1;
		int randomValue = new Random().nextInt(randomSize);
		String selectedMessage = messages.get(randomValue);

		while (lastMessage.contains(selectedMessage)) {
			randomValue = new Random().nextInt(randomSize);
			selectedMessage = messages.get(randomValue);
		}

		if (lastMessage.size() < uniqCount) {
			lastMessage.add(selectedMessage);
		} else {
			lastMessage.remove(0);
			lastMessage.add(selectedMessage);
		}
		return new SendMessage().setText(selectedMessage);

	}

}
