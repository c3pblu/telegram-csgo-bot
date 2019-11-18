package com.telegram.bot.csgo.messages;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bot.csgo.model.Constants;

public class MenuMessage extends SendMessage {
	
	private static final InlineKeyboardMarkup MARK_UP_IN_LINE = new InlineKeyboardMarkup();
	private static final List<List<InlineKeyboardButton>> ROWS_IN_LINE = new ArrayList<>();
	private static final List<InlineKeyboardButton> ROW_1 = new ArrayList<>();
	private static final List<InlineKeyboardButton> ROW_2 = new ArrayList<>();
	private static final List<InlineKeyboardButton> ROW_3 = new ArrayList<>();
	
	static {
		ROW_1.add(new InlineKeyboardButton().setText(Constants.EMOJI_FIRE + " Матчи").setCallbackData(Constants.DATA_MATCHES));
	      ROW_1.add(new InlineKeyboardButton().setText(Constants.EMOJI_TV + " Стримы").setCallbackData(Constants.DATA_STREAMS));
		ROW_1.add(new InlineKeyboardButton().setText(Constants.EMOJI_CUP + " Результаты").setCallbackData(Constants.DATA_RESULTS));
		ROW_2.add(new InlineKeyboardButton().setText(Constants.EMOJI_MIL_MEDAL + "Топ 10").setCallbackData(Constants.DATA_TOP_10));
		ROW_2.add(new InlineKeyboardButton().setText(Constants.EMOJI_MIL_MEDAL + "Топ 20").setCallbackData(Constants.DATA_TOP_20));
		ROW_2.add(new InlineKeyboardButton().setText(Constants.EMOJI_MIL_MEDAL + "Топ 30").setCallbackData(Constants.DATA_TOP_30));
		ROW_3.add(new InlineKeyboardButton().setText(Constants.EMOJI_SPORT_MEDAL + "Топ 10 Игроков").setCallbackData(Constants.DATA_TOP_10_PLAYERS));
		ROW_3.add(new InlineKeyboardButton().setText(Constants.EMOJI_SPORT_MEDAL + "Топ 20 Игроков").setCallbackData(Constants.DATA_TOP_20_PLAYERS));
		ROW_3.add(new InlineKeyboardButton().setText(Constants.EMOJI_SPORT_MEDAL + "Топ 30 Игроков").setCallbackData(Constants.DATA_TOP_30_PLAYERS));
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
