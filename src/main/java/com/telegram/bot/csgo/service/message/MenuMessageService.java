package com.telegram.bot.csgo.service.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bot.csgo.model.CallBackData;
import com.telegram.bot.csgo.model.Emoji;

@Service
public class MenuMessageService {

	public SendMessage menu(String chatId) {
		return SendMessage.builder().replyMarkup(createMenu()).text("Easy Peasy Lemon Squeezy!").chatId(chatId).build();
	}

	private InlineKeyboardMarkup createMenu() {
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		List<InlineKeyboardButton> row2 = new ArrayList<>();
		List<InlineKeyboardButton> row3 = new ArrayList<>();
		List<InlineKeyboardButton> row4 = new ArrayList<>();
		row1.add(InlineKeyboardButton.builder().text(Emoji.FIRE + " Матчи").callbackData(CallBackData.MATCHES).build());
		row1.add(InlineKeyboardButton.builder().text(Emoji.TV + " Стримы").callbackData(CallBackData.STREAMS).build());
		row1.add(InlineKeyboardButton.builder().text(Emoji.CUP + " Результаты").callbackData(CallBackData.RESULTS)
				.build());
		row2.add(InlineKeyboardButton.builder().text(Emoji.SUNGLASSES + "Любимые команды")
				.callbackData(CallBackData.TEAMS).build());
		row2.add(InlineKeyboardButton.builder().text(Emoji.MIC + "Трансляции").callbackData(CallBackData.SCOREBOT)
				.build());
		row3.add(InlineKeyboardButton.builder().text(Emoji.MIL_MEDAL + "Топ 10").callbackData(CallBackData.TOP_10)
				.build());
		row3.add(InlineKeyboardButton.builder().text(Emoji.MIL_MEDAL + "Топ 20").callbackData(CallBackData.TOP_20)
				.build());
		row3.add(InlineKeyboardButton.builder().text(Emoji.MIL_MEDAL + "Топ 30").callbackData(CallBackData.TOP_30)
				.build());
		row4.add(InlineKeyboardButton.builder().text(Emoji.SPORT_MEDAL + "Топ 10 Игроков")
				.callbackData(CallBackData.TOP_10_PLAYERS).build());
		row4.add(InlineKeyboardButton.builder().text(Emoji.SPORT_MEDAL + "Топ 20 Игроков")
				.callbackData(CallBackData.TOP_20_PLAYERS).build());
		row4.add(InlineKeyboardButton.builder().text(Emoji.SPORT_MEDAL + "Топ 30 Игроков")
				.callbackData(CallBackData.TOP_30_PLAYERS).build());
		rowsInLine.addAll(Arrays.asList(row1, row2, row3, row4));
		return InlineKeyboardMarkup.builder().keyboard(rowsInLine).build();

	}

}
