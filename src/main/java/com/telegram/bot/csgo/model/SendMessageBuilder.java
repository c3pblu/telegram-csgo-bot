package com.telegram.bot.csgo.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class SendMessageBuilder {

	private SendMessage result = new SendMessage();

	public SendMessageBuilder text(String text) {
		result.setText(text);
		return this;
	}

	public SendMessageBuilder chatId(Long chatId) {
		result.setChatId(chatId);
		return this;
	}

	public SendMessageBuilder parseMode(String parseMode) {
		result.setParseMode(parseMode);
		return this;
	}

	public SendMessageBuilder replyMarkup(ReplyKeyboard replyMarkup) {
		result.setReplyMarkup(replyMarkup);
		return this;
	}

	public SendMessageBuilder replyToMessageId(Integer replyToMessageId) {
		result.setReplyToMessageId(replyToMessageId);
		return this;
	}

	public SendMessageBuilder disableNotification() {
		result.disableNotification();
		return this;
	}

	public SendMessageBuilder disableWebPagePreview() {
		result.disableWebPagePreview();
		return this;
	}

	public SendMessage build() {
		return result;
	}

}
