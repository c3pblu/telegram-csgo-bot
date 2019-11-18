package com.telegram.bot.csgo.messages;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class NextPage extends SendMessage {

	private InlineKeyboardMarkup markUpInLine = new InlineKeyboardMarkup();
	private List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
	private List<InlineKeyboardButton> row = new ArrayList<>();

	public NextPage(String nextPageId) {
		this.row.add(new InlineKeyboardButton().setText("Next 20 Streams »").setCallbackData(nextPageId));
		this.rowsInLine.add(row);
		this.markUpInLine.setKeyboard(rowsInLine);
		this.setReplyMarkup(markUpInLine);
		this.setText("Go to next Page?");
	}

}
