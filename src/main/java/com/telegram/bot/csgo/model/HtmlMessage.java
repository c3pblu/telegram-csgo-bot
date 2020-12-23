package com.telegram.bot.csgo.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class HtmlMessage extends SendMessage {

	public HtmlMessage(String chatId, Object text) {
		setChatId(chatId);
		setText(String.valueOf(text));
		setDisableWebPagePreview(true);
		setDisableNotification(true);
		setParseMode("html");
	}

}
