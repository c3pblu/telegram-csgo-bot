package com.telegram.bot.csgo.service.message;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bot.csgo.model.CallBackData;
import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.model.SendMessageBuilder;

@Service
public class MenuMessageService {

	public SendMessage menu() {
		return new SendMessageBuilder().replyMarkup(createMenu()).text("Easy Peasy Lemon Squeezy!").build();
	}

	private InlineKeyboardMarkup createMenu() {
		InlineKeyboardMarkup markUpInLine = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		List<InlineKeyboardButton> row2 = new ArrayList<>();
		List<InlineKeyboardButton> row3 = new ArrayList<>();
		List<InlineKeyboardButton> row4 = new ArrayList<>();
		row1.add(new InlineKeyboardButton().setText(Emoji.FIRE + " Матчи").setCallbackData(CallBackData.MATCHES));
		row1.add(new InlineKeyboardButton().setText(Emoji.TV + " Стримы").setCallbackData(CallBackData.STREAMS));
		row1.add(new InlineKeyboardButton().setText(Emoji.CUP + " Результаты").setCallbackData(CallBackData.RESULTS));
		row2.add(new InlineKeyboardButton().setText(Emoji.SUNGLASSES + "Любимые команды")
				.setCallbackData(CallBackData.TEAMS));
		row2.add(new InlineKeyboardButton().setText(Emoji.MIC + "Трансляции").setCallbackData(CallBackData.SCOREBOT));
		row3.add(new InlineKeyboardButton().setText(Emoji.MIL_MEDAL + "Топ 10").setCallbackData(CallBackData.TOP_10));
		row3.add(new InlineKeyboardButton().setText(Emoji.MIL_MEDAL + "Топ 20").setCallbackData(CallBackData.TOP_20));
		row3.add(new InlineKeyboardButton().setText(Emoji.MIL_MEDAL + "Топ 30").setCallbackData(CallBackData.TOP_30));
		row4.add(new InlineKeyboardButton().setText(Emoji.SPORT_MEDAL + "Топ 10 Игроков")
				.setCallbackData(CallBackData.TOP_10_PLAYERS));
		row4.add(new InlineKeyboardButton().setText(Emoji.SPORT_MEDAL + "Топ 20 Игроков")
				.setCallbackData(CallBackData.TOP_20_PLAYERS));
		row4.add(new InlineKeyboardButton().setText(Emoji.SPORT_MEDAL + "Топ 30 Игроков")
				.setCallbackData(CallBackData.TOP_30_PLAYERS));
		rowsInLine.add(row1);
		rowsInLine.add(row2);
		rowsInLine.add(row3);
		rowsInLine.add(row4);
		markUpInLine.setKeyboard(rowsInLine);
		return markUpInLine;
	}

}
