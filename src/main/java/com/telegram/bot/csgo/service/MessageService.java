package com.telegram.bot.csgo.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.CallBackData;
import com.telegram.bot.csgo.model.DbResult;
import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.model.Flag;
import com.telegram.bot.csgo.model.Sticker;
import com.vdurmont.emoji.EmojiParser;

@Component
public class MessageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
	private Map<Long, List<Sticker>> chatLastStickers = new HashMap<>();
	private static final String TEAMS_COMMANDS = "Добавить команду/изменить код страны:\n <b>.команды+Natus Vincere[RU]</b> \nУдалить команду: \n<b>.команды-Natus Vincere</b>";

	@Value("${help.message.file:#{null}}")
	private String helpFile;

	private Dao dao;
	private HttpService httpService;

	@Autowired
	public MessageService(Dao dao, HttpService httpService) {
		this.dao = dao;
		this.httpService = httpService;
	}

	public SendMessage text(Object msg) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.disableNotification();
		sendMessage.disableWebPagePreview();
		sendMessage.setParseMode("html");
		sendMessage.setText(msg.toString());
		return sendMessage;
	}

	public SendMessage nextPage() {
		SendMessage sendMessage = new SendMessage();
		InlineKeyboardMarkup markUpInLine = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
		List<InlineKeyboardButton> row = new ArrayList<>();
		row.add(new InlineKeyboardButton().setText("Next 20 Streams »").setCallbackData("nextPage"));
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
		sendMessage.setParseMode("markdown");
		return sendMessage;
	}

	public SendSticker sticker(Long chatId, Integer uniqCount) {
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
		return new SendSticker().setSticker(selectedSticker.getSticker());
	}

	public SendMessage topTeams(Document doc, Integer count) {
		Elements header = doc.select("div.regional-ranking-header");
		Elements rankedTeams = doc.select("div.ranked-team");
		StringBuilder textMessage = new StringBuilder();
		textMessage.append(Emoji.MIL_MEDAL).append("<b>").append(header.text()).append("</b>\n");
		for (Element team : rankedTeams) {
			if (team.select("span.position").text().equals("#" + (count + 1))) {
				break;
			}
			String teamProfileURL = team.select("div.more").select("a").attr("href");
			Document teamProfile = httpService.getTeamProfile(teamProfileURL);
			String teamFlag = flagUnicodeFromCountry(teamProfile.select("div.team-country").text().trim());

			StringBuilder row = new StringBuilder();
			row.append("<b>").append(team.select("span.position").text()).append("</b> (")
					.append(team.select("div.change").text()).append(") ").append(teamFlag)
					.append("<a href=\'https://hltv.org")
					.append(team.select("div.more").select("a[class=details moreLink]").attr("href")).append("\'>")
					.append(team.select("span.name").text()).append("</a> ").append(team.select("span.points").text())
					.append(" [");
			List<String> listPlayers = new ArrayList<>();
			for (Element player : team.select("div.rankingNicknames")) {
				listPlayers.add(player.text());
			}
			row.append(String.join(", ", listPlayers)).append("]\n");
			textMessage.append(row);
		}

		LOGGER.debug("TopTeams final message:\n{}", textMessage);
		return text(textMessage);
	}

	public SendMessage topPlayers(Document doc, Integer count) {
		Elements rows = doc.select("tr");
		StringBuilder textMessage = new StringBuilder();
		String year = String.valueOf(LocalDate.now().getYear());
		textMessage.append(Emoji.SPORT_MEDAL).append("<b>CS:GO World Top Players ").append(year).append("</b>\n")
				.append("<b>");
		Elements stat = doc.select("tr.stats-table-row").select("th");
		for (int i = 0; i < stat.size(); i++) {
			if (i != stat.size() - 1) {
				textMessage.append(stat.get(i).text().replace("Teams", "Team")).append(", ");
			} else {
				textMessage.append(stat.get(i).text());
			}
		}
		textMessage.append("</b>\n");
		int number = 1;
		for (Element value : rows) {
			if (value.select("td.statsDetail").first() == null) {
				continue;
			}
			if (number > count) {
				break;
			}
			textMessage.append("<b>#").append(number)
					.append(flagUnicodeFromCountry(value.select("td.playerCol").select("img").attr("title")))
					.append("</b> <a href=\'https://hltv.org")
					.append(value.select("td.playerCol").select("a").attr("href")).append("\'>")
					.append(value.select("td.playerCol").text()).append("</a>, ")
					.append(value.select("td.teamCol").select("img").attr("title")).append(", ")
					.append(value.select("td.statsDetail").get(0).text()).append(", ")
					.append(value.select("td.kdDiffCol").text()).append(", ")
					.append(value.select("td.statsDetail").get(1).text()).append(", ")
					.append(value.select("td.ratingCol").text()).append("\n");
			number++;
		}
		LOGGER.debug("TopPlayers final message:\n{}", textMessage);
		return text(textMessage);
	}

	public SendMessage matches(Document doc, Long chatId) {
		StringBuilder textMessage = new StringBuilder();
		// Live Matches
		Elements liveMatches = doc.select("div.liveMatchesContainer").select("div.liveMatches");
		if (!liveMatches.isEmpty()) {
			textMessage.append("<b>Live matches</b>").append(Emoji.EXCL_MARK).append("\n");
			for (Element match : liveMatches.select("div.liveMatch")) {
				textMessage.append(Emoji.CUP).append("<a href=\'https://hltv.org").append(match.select("a").attr("href"))
				.append("\'>").append(match.select("div.matchEventName").text()).append("</a>\n")
				.append(favoriteTeam(chatId, match.select("div.matchTeamName").get(0).text(), true)).append(" ")
				.append(Emoji.VS).append(" ")
				.append(favoriteTeam(chatId, match.select("div.matchTeamName").get(1).text(), true)).append(" (")
				.append(match.select("div.matchMeta").text()).append(") ").append(getUpcomingStars(match))
				.append("\nMatch ID: ").append(match.select("div.liveMatch").attr("data-livescore-match"))
				.append("\n\n");
			}
			
		}
		// Upcoming Matches for today
		Element todayMatches = doc.select("div.upcomingMatchesContainer").select("div.upcomingMatchesSection").first();
		textMessage.append("<b>Upcoming CS:GO matches\n");
		textMessage.append(todayMatches.select("span.matchDayHeadline").text()).append("</b>\n");
		for (Element match : todayMatches.select("div.upcomingMatch")) {
			long unixTime = Long.parseLong(match.select("div.matchTime").attr("data-unix"));
			LocalDateTime localTime = LocalDateTime.ofEpochSecond((unixTime / 1000) + 10800, 0, ZoneOffset.UTC);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
			String formattedTime = localTime.format(formatter);
			textMessage.append("<b>").append(formattedTime).append("</b> - ");
			if (!match.select("div.matchInfoEmpty").isEmpty()) {
				textMessage.append(match.select("div.matchInfoEmpty").text()).append(" ");
			} else {
				textMessage.append(favoriteTeam(chatId, match.select("div.matchTeam.team1").text(), true)).append(" ")
						.append(Emoji.VS).append(" ")
						.append(favoriteTeam(chatId, match.select("div.matchTeam.team2").text(), true)).append(" (")
						.append(match.select("div.matchMeta").text()).append(") ");
			}

			textMessage.append(getUpcomingStars(match)).append(Emoji.SQUARE).append(" ")
					.append("<a href=\'https://hltv.org").append(match.select("a").attr("href")).append("\'>")
					.append(match.select("div.matchEventName").text()).append("</a>\n");

		}

		LOGGER.debug("Matches final message:\n{}", textMessage);
		return text(textMessage);

	}

	public SendMessage results(Document doc, Long chatId) {
		StringBuilder textMessage = new StringBuilder();
		Elements subLists = doc.select("div.results-sublist");
		for (Element resultList : subLists) {
			String headerText = resultList.select("span.standard-headline").text();
			if (headerText.isEmpty()) {
				headerText = "Featured Results";
			}

			textMessage.append(Emoji.CUP).append(" <b>").append(headerText).append("</b>\n");

			for (Element resultCon : resultList.select("div.result-con")) {
				Element team1 = resultCon.select("div.team").get(0);
				Element team2 = resultCon.select("div.team").get(1);
				String team1String = favoriteTeam(chatId, resultCon.select("div.team").get(0).text(), false);
				String team2String = favoriteTeam(chatId, resultCon.select("div.team").get(1).text(), false);

				if (team1.hasClass("team-won")) {
					textMessage.append("<b>").append(team1String).append("</b>");
				} else {
					textMessage.append(team1String);
				}

				textMessage.append(" [").append(resultCon.select("td.result-score").text()).append("] ");

				if (team2.hasClass("team-won")) {
					textMessage.append("<b>").append(team2String).append("</b>");
				} else {
					textMessage.append(team2String);
				}

				textMessage.append(" (").append(resultCon.select("div.map-text").text()).append(") ")
						.append(getStars(resultCon)).append(Emoji.SQUARE).append(" ")
						.append("<a href=\'https://hltv.org").append(resultCon.select("a").attr("href")).append("\'>")
						.append(resultCon.select("td.event").text()).append("</a> \n");

			}
			textMessage.append("\n");

			if (headerText.startsWith("Results"))
				break;
		}

		LOGGER.debug("Results final message:\n{}", textMessage);
		return text(textMessage);
	}

	public SendMessage streams(JSONObject json) {
		StringBuilder textMessage = new StringBuilder();
		textMessage.append("<b>Live</b>").append(Emoji.EXCL_MARK).append("<b>Streams on Twitch:</b>\n");
		JSONArray arr = json.getJSONArray("data");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject data = arr.getJSONObject(i);
			textMessage.append("<b>(").append(data.getNumber("viewer_count")).append(")</b> ")
					.append("<a href=\'https://www.twitch.tv/").append(data.getString("user_name")).append("\'>")
					.append(data.getString("user_name")).append("</a> ")
					.append(flagUnicodeFromCountry(data.getString("language").toUpperCase())).append(" ")
					.append(data.getString("title").replace("<", "").replace(">", "")).append("\n");
		}
		LOGGER.debug("Streams final message:\n{}", textMessage);
		return text(textMessage);

	}

	public SendMessage cite(Document doc) {
		StringBuilder text = new StringBuilder();
		text.append(doc.select("cite").text());
		String athor = doc.select("small").text();
		if (!StringUtils.isEmpty(athor)) {
			text.append("\n<b>").append(athor).append("</b>");
		}
		return text(text);
	}

	public SendMessage dbResult(String dbResult, String name) {
		switch (dbResult) {
		case DbResult.FLAG_NOT_FOUND:
			return text(DbResult.NOTHING_WAS_CHANGED);
		case DbResult.NOTHING_WAS_CHANGED:
			return text(DbResult.NOTHING_WAS_CHANGED);
		case DbResult.UPDATED:
			return text("<b>" + name + "</b> " + DbResult.UPDATED);
		case DbResult.ALREADY_EXIST:
			return text("<b>" + name + "</b> " + DbResult.ALREADY_EXIST);
		case DbResult.INSERTED:
			return text("<b>" + name + "</b> " + DbResult.INSERTED);
		case DbResult.DELETED:
			return text("<b>" + name + "</b> " + DbResult.DELETED);
		default:
			return text(DbResult.OOPS);
		}
	}

	public SendMessage favoriteTeams(List<FavoriteTeam> teams, Long chatId) {
		StringBuilder textMessage = new StringBuilder();
		if (teams.isEmpty())
			return text(Emoji.INFO + " Ваши любимые команды:\n\n<b>У вас пока нет любимых команд!</b> " + Emoji.SAD
					+ "\n\n" + TEAMS_COMMANDS);
		textMessage.append(Emoji.INFO).append(" Ваши любимые команды:\n\n");
		teams.stream()
				.forEach(team -> textMessage.append("<b>").append(team.getName()).append("</b> [")
						.append(team.getCountryCode().getCode()).append("] ")
						.append(flagUnicodeFromCountry(team.getCountryCode().getCode())).append("\n"));
		textMessage.append("\n").append(TEAMS_COMMANDS);
		return text(textMessage);
	}

	public SendMessage scorebot(JSONObject json) {
		StringBuilder textMessage = new StringBuilder();
		String logType = json.keys().next();
		LOGGER.debug("LogType: {}", logType);
		// Round Start
		if (logType.equals("RoundStart")) {
			textMessage.append("<b>Round Started</b>");
		}
		// Round End
		if (logType.equals("RoundEnd")) {
			String winner = json.query("/RoundEnd/winner").toString();
			String winType = json.query("/RoundEnd/winType").toString();
			String ctScore = json.query("/RoundEnd/counterTerroristScore").toString();
			String tScore = json.query("/RoundEnd/terroristScore").toString();
			textMessage.append("<b>Round Over: ");
			if (winner.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE).append("T");
			} else {
				textMessage.append(Emoji.DIMOND_BLUE).append("CT");
			}
			textMessage.append(" Win - ");
			switch (winType) {
			case "Target_Bombed":
				textMessage.append("Target Bombed");
				break;
			case "Bomb_Defused":
				textMessage.append("Bomb Defused");
				break;
			case "Target_Saved":
				textMessage.append("Target Saved");
				break;
			default:
				textMessage.append("Enemy Eliminated");
				break;
			}
			textMessage.append("\n").append(Emoji.DIMOND_ORANGE).append(tScore).append(" - ").append(Emoji.DIMOND_BLUE)
					.append(ctScore).append("</b>");

		}
		// Kill
		if (logType.equals("Kill")) {
			String killerSide = json.query("/Kill/killerSide").toString();
			String killerNick = json.query("/Kill/killerNick").toString();
			String victimSide = json.query("/Kill/victimSide").toString();
			String victimNick = json.query("/Kill/victimNick").toString();
			String weapon = json.query("/Kill/weapon").toString();
			boolean isHeadShot = (boolean) json.query("/Kill/headShot");
			if (killerSide.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE);
			} else {
				textMessage.append(Emoji.DIMOND_BLUE);
			}
			textMessage.append("<b>").append(killerNick).append("</b> killed");
			if (victimSide.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE);
			} else {
				textMessage.append(Emoji.DIMOND_BLUE);
			}
			textMessage.append("<b>").append(victimNick).append("</b> with ").append(weapon);
			if (isHeadShot) {
				textMessage.append(" ").append(Emoji.HELM);
			}
		}

		// Bomb Planted
		if (logType.equals("BombPlanted")) {
			String playerNick = json.query("/BombPlanted/playerNick").toString();
			String tPlayers = json.query("/BombPlanted/tPlayers").toString();
			String ctPlayers = json.query("/BombPlanted/ctPlayers").toString();
			textMessage.append(Emoji.DIMOND_ORANGE).append("<b>").append(playerNick).append(" ").append(Emoji.BOMB)
					.append(" planted the bomb").append(Emoji.DIMOND_ORANGE).append(tPlayers).append(" on")
					.append(Emoji.DIMOND_BLUE).append(ctPlayers).append("</b>");
		}
		// Bomb Defused
		if (logType.equals("BombDefused")) {
			String playerNick = json.query("/BombDefused/playerNick").toString();
			textMessage.append(Emoji.DIMOND_BLUE).append("<b>").append(playerNick).append(" defused the bomb</b>");
		}
		// Suicide
		if (logType.equals("Suicide")) {
			String playerNick = json.query("/Suicide/playerNick").toString();
			String side = json.query("/Suicide/side").toString();
			if (side.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE);
			} else {
				textMessage.append(Emoji.DIMOND_BLUE);
			}
			textMessage.append("<b>").append(playerNick).append("</b> committed suicide");
		}
		// PlayerJoin
		if (logType.equals("PlayerJoin")) {
			String playerNick = json.query("/PlayerJoin/playerNick").toString();
			textMessage.append("<b>").append(playerNick).append("</b> joined the game");
		}
		// PlayerQuit
		if (logType.equals("PlayerQuit")) {
			String playerNick = json.query("/PlayerQuit/playerNick").toString();
			String playerSide = json.query("/PlayerQuit/playerSide").toString();
			if (playerSide.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE);
			}
			if (playerSide.equals("CT")) {
				textMessage.append(Emoji.DIMOND_BLUE);
			}
			textMessage.append("<b>").append(playerNick).append("</b> quit the game");
		}
		// MatchStarted
		if (logType.equals("MatchStarted")) {
			String map = json.query("/MatchStarted/map").toString();
			textMessage.append("<b>Match Started: ").append(map).append("</b>");
		}
		LOGGER.debug("Log Message: {}", textMessage);
		return text(textMessage.toString());
	}

	public SendMessage teamsFormat() {
		return text("Неверный формат!\nСмотрите примеры ниже!\n\n" + TEAMS_COMMANDS);
	}

	public SendMessage matchesForToday() {
		return text("Ближайшие матчи:");
	}

	public SendMessage resultsForToday() {
		return text("Последние результаты:");
	}

	public SendMessage oops() {
		return text("Упс, ты слишком долго думал парень!");
	}

	public SendMessage stoped() {
		return text("Трансляция остановлена");
	}

	public SendMessage scorebot() {
		return text(Emoji.INFO
				+ " Для запуска трансляции:\n.<b>старт-1234567</b> (где 1234567 это Match ID - его можно посмотреть в .мачти)\n<b>.стоп</b> - остановить трансяцию");
	}

	private StringBuilder getStars(Element match) {
		StringBuilder stars = new StringBuilder();
		match.select("div.stars").select("i").stream().forEach(star -> stars.append(Emoji.STAR));
		return stars;
	}

	private StringBuilder getUpcomingStars(Element match) {
		StringBuilder stars = new StringBuilder();
		for (int i = 0; i < 5 - match.select("i.fa.fa-star.faded").size(); i++) {
			stars.append(Emoji.STAR);
		}
		return stars;
	}

	private String favoriteTeam(Long chatId, String name, boolean isBold) {
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

	public String flagUnicodeFromCountry(String country) {
		String text = null;
		Flag ourFlag = new Flag();
		List<Flag> flags = dao.getFlags();
		if (country.matches("[A-Z][A-Z]")) {
			ourFlag = flags.parallelStream().filter(t -> t.getCode().equals(country.toUpperCase())).findFirst()
					.orElse(null);
		} else {
			ourFlag = flags.parallelStream().filter(t -> t.getName().equals(country)).findFirst().orElse(null);
		}

		if (!StringUtils.isBlank(ourFlag.getUnicode())) {
			text = StringEscapeUtils.unescapeJava(ourFlag.getUnicode());
		} else {
			text = EmojiParser.parseToUnicode(ourFlag.getEmojiCode());
		}

		if (text == null)
			text = EmojiParser.parseToUnicode(":un:");
		LOGGER.debug("Country code: {}, Emoji code: {}", country, ourFlag.getEmojiCode());
		return text;
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

	private String helpText() {
		String helpMessage = "Не найден файл описания помощи!";
		if (helpFile == null) {
			return helpMessage;
		}
		try {
			List<String> lines = Files.readAllLines(Paths.get(helpFile), StandardCharsets.UTF_8);
			helpMessage = String.join(System.lineSeparator(), lines);
		} catch (IOException e) {
			LOGGER.error("File for Help message not found! See 'help.message.file' property");
		}
		return helpMessage;
	}
}
