package com.telegram.bot.csgo.service.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.DbResult;
import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.model.Flag;
import com.telegram.bot.csgo.model.Sticker;
import com.vdurmont.emoji.EmojiParser;

@Service
public class MessageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
	private static final String TEAMS_COMMANDS = "Добавить команду/изменить код страны:\n <b>.команды+Natus Vincere[RU]</b> \nУдалить команду: \n<b>.команды-Natus Vincere</b>";
	private Map<String, List<Sticker>> chatLastStickers = new HashMap<>();

	private Dao dao;
	private HelpMessageService helpMessageService;
	private MenuMessageService menuMessageService;

	@Autowired
	public MessageService(Dao dao, HelpMessageService helpMessageService, MenuMessageService menuMessageService) {
		this.dao = dao;
		this.helpMessageService = helpMessageService;
		this.menuMessageService = menuMessageService;

	}

	public SendMessage helpMessage(String chatId) {
		return helpMessageService.help(chatId);
	}

	public SendMessage menuMessage(String chatId) {
		return menuMessageService.menu(chatId);
	}

	public SendMessage htmlMessage(String chatId, Object msg) {
		return SendMessage.builder().disableNotification(true).disableWebPagePreview(true).parseMode("html")
				.chatId(chatId).text(String.valueOf(msg)).build();
	}

	public SendSticker sticker(String chatId, Integer uniqCount) {
		List<Sticker> stickers = dao.getStickers();
		int randomSize = stickers.size() - 1 + 1;
		int randomValue = new Random().nextInt(randomSize);
		Sticker selectedSticker = stickers.get(randomValue);
		List<Sticker> lastStickers = chatLastStickers.get(chatId) != null ? chatLastStickers.get(chatId)
				: new ArrayList<>();

		while (lastStickers.contains(selectedSticker)) {
			randomValue = new Random().nextInt(randomSize);
			selectedSticker = stickers.get(randomValue);
		}

		if (lastStickers.size() < uniqCount) {
			lastStickers.add(selectedSticker);
		} else {
			lastStickers.remove(0);
			lastStickers.add(selectedSticker);
		}
		chatLastStickers.put(chatId, lastStickers);
		return SendSticker.builder().sticker(new InputFile(selectedSticker.getSticker())).chatId(chatId).build();
	}

	public SendMessage dbResult(String chatId, String dbResult, String name) {
		switch (dbResult) {
		case DbResult.FLAG_NOT_FOUND:
			return htmlMessage(chatId, DbResult.NOTHING_WAS_CHANGED);
		case DbResult.NOTHING_WAS_CHANGED:
			return htmlMessage(chatId, DbResult.NOTHING_WAS_CHANGED);
		case DbResult.UPDATED:
			return htmlMessage(chatId, "<b>" + name + "</b> " + DbResult.UPDATED);
		case DbResult.ALREADY_EXIST:
			return htmlMessage(chatId, "<b>" + name + "</b> " + DbResult.ALREADY_EXIST);
		case DbResult.INSERTED:
			return htmlMessage(chatId, "<b>" + name + "</b> " + DbResult.INSERTED);
		case DbResult.DELETED:
			return htmlMessage(chatId, "<b>" + name + "</b> " + DbResult.DELETED);
		default:
			return htmlMessage(chatId, DbResult.OOPS);
		}
	}

	public SendMessage cite(String chatId, Document doc) {
		StringBuilder text = new StringBuilder();
		text.append(doc.select("cite").text());
		String athor = doc.select("small").text();
		if (!StringUtils.isEmpty(athor)) {
			text.append("\n<b>").append(athor).append("</b>");
		}
		return htmlMessage(chatId, text);
	}

	public SendMessage teamsFormat(String chatId) {
		return htmlMessage(chatId, "Неверный формат!\nСмотрите примеры ниже!\n\n" + TEAMS_COMMANDS);
	}

	public SendMessage oops(String chatId) {
		return htmlMessage(chatId, "Упс, ты слишком долго думал парень!");
	}

	public SendMessage stoped(String chatId) {
		return htmlMessage(chatId, "Трансляция остановлена");
	}

	public SendMessage scorebot(String chatId) {
		return htmlMessage(chatId, Emoji.INFO
				+ " Для запуска трансляции:\n.<b>старт-1234567</b> (где 1234567 это Match ID - его можно посмотреть в .мачти)\n<b>.стоп</b> - остановить трансяцию");
	}

	public String flagUnicodeFromCountry(String country) {
		String text = null;
		if (country == null) {
			text = EmojiParser.parseToUnicode(":un:");
			LOGGER.debug("Country code: {}, Emoji code: {}", country, ":un: (default)");
			return text;
		}
		Flag ourFlag = new Flag();
		List<Flag> flags = dao.getFlags();
		if (country.matches("[A-Z][A-Z]")) {
			ourFlag = flags.parallelStream().filter(t -> t.getCode().equals(country.toUpperCase())).findFirst()
					.orElse(null);
		} else {
			ourFlag = flags.parallelStream().filter(t -> t.getName().equals(country)).findFirst().orElse(null);
		}

		if (ourFlag != null) {
			if (!StringUtils.isBlank(ourFlag.getUnicode())) {
				text = StringEscapeUtils.unescapeJava(ourFlag.getUnicode());
			} else {
				text = EmojiParser.parseToUnicode(ourFlag.getEmojiCode());
			}
			LOGGER.debug("Country code: {}, Emoji code: {}", country, ourFlag.getEmojiCode());
		}
		if (text == null) {
			text = EmojiParser.parseToUnicode(":un:");
			LOGGER.debug("Country code: {}, Emoji code: {}", country, ":un: (default)");
		}
		return text;
	}

	public String favoriteTeam(String chatId, String name, boolean isBold) {
		String teamName = name;
		FavoriteTeam fvTeam = dao.getTeams(chatId).parallelStream()
				.filter(team -> team.getChatId().equals(chatId) && team.getName().equalsIgnoreCase(name)).findFirst()
				.orElse(null);
		if (fvTeam != null) {
			String flag = flagUnicodeFromCountry(fvTeam.getCountryCode().getCode());
			if (!isBold) {
				teamName = flag + teamName;
			} else {
				teamName = flag + "<b>" + teamName + "</b>";
			}
		}
		return unlinkName(teamName);
	}

	private String unlinkName(String name) {
		if (name.contains(".")) {
			name = name.replace('.', ',');
		}
		return name;
	}

}
