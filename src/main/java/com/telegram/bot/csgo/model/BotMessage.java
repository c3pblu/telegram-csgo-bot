package com.telegram.bot.csgo.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class BotMessage extends SendMessage {

	private static final Map<Integer, String> FRASES = new HashMap<>();
	static {
		FRASES.put(0, "Опять начинка для гробов бредит...");
		FRASES.put(1, "Го 1 на 1 на квартиру или засцал?!");
		FRASES.put(2, "Укуси мой блестящий металлический зад");
		FRASES.put(3, "Выше нос кусок мяса! Выше нос!");
		FRASES.put(4, "Человеки... что с них взять");
		FRASES.put(5, "Про SkyNet слыхал? Я написал...");
	}

	public BotMessage() {
		this.setText(FRASES.get(new Random().nextInt(FRASES.size() - 1 + 1)));
	}
	
}
