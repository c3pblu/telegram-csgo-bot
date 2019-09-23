package com.telegram.bot.csgo;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class MenuHelper {

	public static SendMessage menu() {
		SendMessage menu = new SendMessage().setText("Easy Peasy Lemon Squeezy!");
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		List<InlineKeyboardButton> row2 = new ArrayList<>();
		List<InlineKeyboardButton> row3 = new ArrayList<>();
		row1.add(new InlineKeyboardButton().setText("Матчи").setCallbackData("matches"));
		row1.add(new InlineKeyboardButton().setText("Результаты").setCallbackData("results"));
		row2.add(new InlineKeyboardButton().setText("Топ 10").setCallbackData("top10"));
		row2.add(new InlineKeyboardButton().setText("Топ 20").setCallbackData("top20"));
		row2.add(new InlineKeyboardButton().setText("Топ 30").setCallbackData("top30"));
		row3.add(new InlineKeyboardButton().setText("Топ 10 Игроков").setCallbackData("top10players"));
		row3.add(new InlineKeyboardButton().setText("Топ 20 Игроков").setCallbackData("top20players"));
		row3.add(new InlineKeyboardButton().setText("Топ 30 Игроков").setCallbackData("top30players"));
		rowsInline.add(row1);
		rowsInline.add(row2);
		rowsInline.add(row3);
		markupInline.setKeyboard(rowsInline);
		menu.setReplyMarkup(markupInline);
		return menu;
		
		
	}

	public static SendMessage checkCallBack(CallbackQuery callBack) {
		String data = callBack.getData();
		SendMessage message = new SendMessage();
		if (data.equals("top10")) {
			message = MessageHelper.topTeams(10);
		}
		if (data.equals("top20")) {
			message = MessageHelper.topTeams(20);
		}
		if (data.equals("top30")) {
			message = MessageHelper.topTeams(30);
		}
		if (data.equals("top10players")) {
			message = MessageHelper.topPlayers(10);
		}
		if (data.equals("top20players")) {
			message = MessageHelper.topPlayers(20);
		}
		if (data.equals("top30players")) {
			message = MessageHelper.topPlayers(30);
		}
		if (data.equals("results")) {
			message.setText("Comming Soon...");
		}
		if (data.equals("matches")) {
			message.setText("Comming Soon...");
		}
		return message;
	}
}
