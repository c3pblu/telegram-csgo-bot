package com.telegram.bot.csgo.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.model.Constants;

public class TextMessage extends SendMessage {

	public TextMessage(String msg) {
		this.disableNotification();
		this.disableWebPagePreview();
		this.setParseMode(Constants.HTML);
		this.setText(msg);
	}
}
