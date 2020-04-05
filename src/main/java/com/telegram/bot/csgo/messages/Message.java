package com.telegram.bot.csgo.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bot.csgo.model.CallBackData;
import com.telegram.bot.csgo.model.Commands;
import com.telegram.bot.csgo.model.Emoji;

@Component
public class Message {

	private static final String PARSE_MODE_HTML = "html";
	private static final ArrayList<String> STICKERS = initStickers();
	private static ArrayList<String> lastSticker = new ArrayList<>();

	public SendMessage createTextMessage(Object msg) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.disableNotification();
		sendMessage.disableWebPagePreview();
		sendMessage.setParseMode(PARSE_MODE_HTML);
		sendMessage.setText(msg.toString());
		return sendMessage;
	}

	public SendMessage createNextPageMessage(String nextPageId) {
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

	public SendMessage createMenuMessage() {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setReplyMarkup(createMenu());
		sendMessage.setText("Easy Peasy Lemon Squeezy!");
		return sendMessage;
	}

	public SendMessage createHelpMessage() {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setText(helpText());
		return sendMessage;
	}

	public SendSticker createBotMessage(Integer uniqCount) {
		int randomSize = STICKERS.size() - 1 + 1;
		int randomValue = new Random().nextInt(randomSize);
		String selectedMessage = STICKERS.get(randomValue);

		while (lastSticker.contains(selectedMessage)) {
			randomValue = new Random().nextInt(randomSize);
			selectedMessage = STICKERS.get(randomValue);
		}

		if (lastSticker.size() < uniqCount) {
			lastSticker.add(selectedMessage);
		} else {
			lastSticker.remove(0);
			lastSticker.add(selectedMessage);
		}
		return new SendSticker().setSticker(selectedMessage);
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

	private static ArrayList<String> initStickers() {
		ArrayList<String> stickers = new ArrayList<>();
		stickers.add("CAADAgADIQAD9mOfG2LGtCrsw7bFFgQ");
		stickers.add("CAADAgADHgAD9mOfG7X25hWYzpI1FgQ");
		stickers.add("CAADAgADHwAD9mOfG_Ba2iIqOnazFgQ");
		stickers.add("CAADAgADIgAD9mOfG4hfcToK4DCYFgQ");
		stickers.add("CAADAgADJQAD9mOfG963ItgypxoIFgQ");
		stickers.add("CAADAgADKgAD9mOfG7fCrBPbLEDJFgQ");
		stickers.add("CAADAgADFAAD9mOfGxFXaqquJHwYFgQ");
		stickers.add("CAADAgADEQAD9mOfG94SbA2pBiwnFgQ");
		stickers.add("CAADAgADCwAD9mOfG8RskvZsrlZsFgQ");
		stickers.add("CAADAgADDQAD9mOfGxyG9FhomVn0FgQ");
		stickers.add("CAADAgADBwAD9mOfGwvUQUWU0Bv_FgQ");
		stickers.add("CAADAgADBgAD9mOfG-4M62fmXafEFgQ");
		stickers.add("CAADAgADAwAD9mOfGzeICv_hr6IOFgQ");
		stickers.add("CAADAgADCgAD9mOfG91GVm2tjQaEFgQ");
		stickers.add("CAADAgADJAAD9mOfGw-taxRFVDWeFgQ");
		stickers.add("CAADAgADKAAD9mOfG-AyIRVUq8l0FgQ");
		stickers.add("CAADAgADJgAD9mOfG-rYuchCMU8-FgQ");
		stickers.add("CAADAgADHAAD9mOfG8QGYzOYAXv9FgQ");
		stickers.add("CAADAgADFgAD9mOfG7tdoHpun4KJFgQ");
		stickers.add("CAADAgADIwAD9mOfG7uEav_8NSjTFgQ");
		return stickers;
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
