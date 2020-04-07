package com.telegram.bot.csgo.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.CallBackData;
import com.telegram.bot.csgo.model.Commands;
import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.model.Sticker;

@Component
public class Message {
	
	@Autowired
	private Dao dao;

	private static final String PARSE_MODE_HTML = "html";
	private ArrayList<Sticker> lastSticker = new ArrayList<>();

	public SendMessage text(Object msg) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.disableNotification();
		sendMessage.disableWebPagePreview();
		sendMessage.setParseMode(PARSE_MODE_HTML);
		sendMessage.setText(msg.toString());
		return sendMessage;
	}

	public SendMessage nextPage(String nextPageId) {
		SendMessage sendMessage = new SendMessage();
		InlineKeyboardMarkup markUpInLine = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
		List<InlineKeyboardButton> row = new ArrayList<>();
		row.add(new InlineKeyboardButton().setText("Next 20 Streams »").setCallbackData(nextPageId));
		rowsInLine.add(row);
		markUpInLine.setKeyboard(rowsInLine);
		sendMessage.setReplyMarkup(markUpInLine);
		sendMessage.setText("Go to next Page?");
		return sendMessage;
	}

	public SendMessage menu() {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setReplyMarkup(createMenu());
		sendMessage.setText("Easy Peasy Lemon Squeezy!");
		return sendMessage;
	}

	public SendMessage help() {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setText(helpText());
		return sendMessage;
	}

	public SendSticker sticker(Integer uniqCount) {
		List<Sticker> stickers = dao.getStickers();
		int randomSize = stickers.size() - 1 + 1;
		int randomValue = new Random().nextInt(randomSize);
		Sticker selectedSticker = stickers.get(randomValue);

		while (lastSticker.contains(selectedSticker)) {
			randomValue = new Random().nextInt(randomSize);
			selectedSticker = stickers.get(randomValue);
		}

		if (lastSticker.size() < uniqCount) {
			lastSticker.add(selectedSticker);
		} else {
			lastSticker.remove(0);
			lastSticker.add(selectedSticker);
		}
		return new SendSticker().setSticker(selectedSticker.getSticker());
	}

	private InlineKeyboardMarkup createMenu() {
		InlineKeyboardMarkup markUpInLine = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		List<InlineKeyboardButton> row2 = new ArrayList<>();
		List<InlineKeyboardButton> row3 = new ArrayList<>();
		List<InlineKeyboardButton> row4 = new ArrayList<>();
		row1.add(new InlineKeyboardButton().setText(Emoji.FIRE.getCode() + " Матчи")
				.setCallbackData(CallBackData.MATCHES.getName()));
		row1.add(new InlineKeyboardButton().setText(Emoji.TV.getCode() + " Стримы")
				.setCallbackData(CallBackData.STREAMS.getName()));
		row1.add(new InlineKeyboardButton().setText(Emoji.CUP.getCode() + " Результаты")
				.setCallbackData(CallBackData.RESULTS.getName()));
		row2.add(new InlineKeyboardButton().setText(Emoji.MIL_MEDAL.getCode() + "Топ 10")
				.setCallbackData(CallBackData.TOP_10.getName()));
		row2.add(new InlineKeyboardButton().setText(Emoji.MIL_MEDAL.getCode() + "Топ 20")
				.setCallbackData(CallBackData.TOP_20.getName()));
		row2.add(new InlineKeyboardButton().setText(Emoji.MIL_MEDAL.getCode() + "Топ 30")
				.setCallbackData(CallBackData.TOP_30.getName()));
		row3.add(new InlineKeyboardButton().setText(Emoji.SPORT_MEDAL.getCode() + "Топ 10 Игроков")
				.setCallbackData(CallBackData.TOP_10_PLAYERS.getName()));
		row3.add(new InlineKeyboardButton().setText(Emoji.SPORT_MEDAL.getCode() + "Топ 20 Игроков")
				.setCallbackData(CallBackData.TOP_20_PLAYERS.getName()));
		row3.add(new InlineKeyboardButton().setText(Emoji.SPORT_MEDAL.getCode() + "Топ 30 Игроков")
				.setCallbackData(CallBackData.TOP_30_PLAYERS.getName()));
		row4.add(new InlineKeyboardButton().setText(Emoji.SUNGLASSES.getCode() + "Любимые команды")
				.setCallbackData(CallBackData.TEAMS.getName()));
		rowsInLine.add(row1);
		rowsInLine.add(row2);
		rowsInLine.add(row3);
		rowsInLine.add(row4);
		markUpInLine.setKeyboard(rowsInLine);
		return markUpInLine;
	}


	private String helpText() {
		return Emoji.INFO.getCode() + " Могу посмотреть, что там нового на HLTV.org\n" 
				+ "Спрашивай, не стесняйся:\n"
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.HELP.getName() + "* - Эта информация\n" 
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.MENU.getName() + "* - Меню\n"
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.MATCHES.getName() + "* - Текущие/ближайшие матчи\n"
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.STREAMS.getName() + "* - Стримы Twitch.tv\n"
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.RESULTS.getName() + "* - Результаты матчей \n"
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.TOP_10.getName() + "* - Top 10 Команд\n"
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.TOP_20.getName() + "* - Top 20 Команд\n" 
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.TOP_30.getName() + "* - Top 30 Команд\n"
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.TOP_10_PLAYERS.getName() + "* - Top 10 Игроков\n" 
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.TOP_20_PLAYERS.getName() + "* - Top 20 Игроков\n"
				+ Emoji.HEAVY_CHECK_MARK.getCode() + " *" + Commands.TOP_30_PLAYERS.getName() + "* - Top 30 Игроков";
	}
}
