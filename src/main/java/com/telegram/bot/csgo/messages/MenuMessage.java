package com.telegram.bot.csgo.messages;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class MenuMessage extends SendMessage {
	
	private static final InlineKeyboardMarkup MARK_UP_IN_LINE = new InlineKeyboardMarkup();
	private static final List<List<InlineKeyboardButton>> ROWS_IN_LINE = new ArrayList<>();
	private static final List<InlineKeyboardButton> ROW_1 = new ArrayList<>();
	private static final List<InlineKeyboardButton> ROW_2 = new ArrayList<>();
	private static final List<InlineKeyboardButton> ROW_3 = new ArrayList<>();
	
	static {
		ROW_1.add(new InlineKeyboardButton().setText(Emoji.FIRE.getCode() + " Матчи").setCallbackData(CallBackData.MATCHES.getName()));
	      ROW_1.add(new InlineKeyboardButton().setText(Emoji.TV.getCode() + " Стримы").setCallbackData(CallBackData.STREAMS.getName()));
		ROW_1.add(new InlineKeyboardButton().setText(Emoji.CUP.getCode() + " Результаты").setCallbackData(CallBackData.RESULTS.getName()));
		ROW_2.add(new InlineKeyboardButton().setText(Emoji.MIL_MEDAL.getCode() + "Топ 10").setCallbackData(CallBackData.TOP_10.getName()));
		ROW_2.add(new InlineKeyboardButton().setText(Emoji.MIL_MEDAL.getCode() + "Топ 20").setCallbackData(CallBackData.TOP_20.getName()));
		ROW_2.add(new InlineKeyboardButton().setText(Emoji.MIL_MEDAL.getCode() + "Топ 30").setCallbackData(CallBackData.TOP_30.getName()));
		ROW_3.add(new InlineKeyboardButton().setText(Emoji.SPORT_MEDAL.getCode() + "Топ 10 Игроков").setCallbackData(CallBackData.TOP_10_PLAYERS.getName()));
		ROW_3.add(new InlineKeyboardButton().setText(Emoji.SPORT_MEDAL.getCode() + "Топ 20 Игроков").setCallbackData(CallBackData.TOP_20_PLAYERS.getName()));
		ROW_3.add(new InlineKeyboardButton().setText(Emoji.SPORT_MEDAL.getCode() + "Топ 30 Игроков").setCallbackData(CallBackData.TOP_30_PLAYERS.getName()));
		ROWS_IN_LINE.add(ROW_1);
		ROWS_IN_LINE.add(ROW_2);
		ROWS_IN_LINE.add(ROW_3);
		MARK_UP_IN_LINE.setKeyboard(ROWS_IN_LINE);
	}
	
	public MenuMessage() {
		this.setReplyMarkup(MARK_UP_IN_LINE);
		this.setText("Easy Peasy Lemon Squeezy!");
	}

}
