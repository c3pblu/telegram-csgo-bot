package com.telegram.bot.csgo.helper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public final class MenuHelper {
	
	private static final String TOP_10 = "top10";
	private static final String TOP_20 = "top20";
	private static final String TOP_30 = "top30";
	private static final String TOP_10_PLAYERS = "top10players";
	private static final String TOP_20_PLAYERS = "top20players";
	private static final String TOP_30_PLAYERS = "top30players";
	private static final String MATCHES = "matches";
	private static final String RESULTS = "results";
	
	private MenuHelper() {
		
	}

	public static SendMessage menu() {
		SendMessage menu = new SendMessage().setText("Easy Peasy Lemon Squeezy!");
		InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		List<InlineKeyboardButton> row2 = new ArrayList<>();
		List<InlineKeyboardButton> row3 = new ArrayList<>();
		row1.add(new InlineKeyboardButton().setText("Матчи").setCallbackData(MATCHES));
		row1.add(new InlineKeyboardButton().setText("Результаты").setCallbackData(RESULTS));
		row2.add(new InlineKeyboardButton().setText("Топ 10").setCallbackData(TOP_10));
		row2.add(new InlineKeyboardButton().setText("Топ 20").setCallbackData(TOP_20));
		row2.add(new InlineKeyboardButton().setText("Топ 30").setCallbackData(TOP_30));
		row3.add(new InlineKeyboardButton().setText("Топ 10 Игроков").setCallbackData(TOP_10_PLAYERS));
		row3.add(new InlineKeyboardButton().setText("Топ 20 Игроков").setCallbackData(TOP_20_PLAYERS));
		row3.add(new InlineKeyboardButton().setText("Топ 30 Игроков").setCallbackData(TOP_30_PLAYERS));
		rowsInline.add(row1);
		rowsInline.add(row2);
		rowsInline.add(row3);
		markupInline.setKeyboard(rowsInline);
		menu.setReplyMarkup(markupInline);
		return menu;

	}

	public static SendMessage checkCallBack(CallbackQuery callBack) {
		long responseTime = Instant.now().getEpochSecond() - callBack.getMessage().getDate();
		if (responseTime > 300) {
			return new SendMessage().setText("Упс, ты слишком долго думал парень!");
		}
		
		String data = callBack.getData();
		
		if (data.equals(TOP_10)) {
			return MessageHelper.topTeams(10);
		}
		if (data.equals(TOP_20)) {
			return MessageHelper.topTeams(20);
		}
		if (data.equals(TOP_30)) {
			return MessageHelper.topTeams(30);
		}
		if (data.equals(TOP_10_PLAYERS)) {
			return MessageHelper.topPlayers(10);
		}
		if (data.equals(TOP_20_PLAYERS)) {
			return MessageHelper.topPlayers(20);
		}
		if (data.equals(TOP_30_PLAYERS)) {
			return MessageHelper.topPlayers(30);
		}
		if (data.equals(RESULTS)) {
			return new SendMessage().setText("Comming Soon...");
		}
		if (data.equals(MATCHES)) {
			return new SendMessage().setText("Comming Soon...");
		}
		return new SendMessage();
	}
}
