package com.telegram.bot.csgo.messages;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.DbResult;
import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.model.Flag;
import com.telegram.bot.csgo.model.Streams;
import com.vdurmont.emoji.EmojiParser;

@Component
public class MessageBuilder {

	private static final String MATCHES_FOR_TODAY = "Ближайшие матчи:";
	private static final String RESULTS_FOR_TODAY = "Последние результаты:";
	private static final String TEAMS_COMMANDS = "Добавить команду/изменить код страны:\n <b>.команды+Natus Vincere[RU]</b> \nУдалить команду: \n<b>.команды-Natus Vincere</b>";
	private static final String TEAMS_DESCRIPTION = Emoji.INFO.getCode()
			+ " Любимая команда выделяется флагом в результатах и матчах (+ толстый шрифт)";
	private final static String USER_AGENT_NAME = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
	private final static String HLTV = "https://www.hltv.org";
	private final static String CITES = "https://api.forismatic.com/api/1.0/?method=getQuote&format=html&lang=ru";
	private final static String TWITCH = "https://api.twitch.tv/helix/streams?game_id=32399&language=en&language=ru";
	private final static String CLIENT_ID = "Client-ID";
	private final static String EXCEPTION_MSG = "Can't get data from site";

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageBuilder.class);
	private static final HttpClient CLIENT = new HttpClient();
	@Value("${twitch.client.id}")
	private String clientId;

	@Autowired
	private TeamFlagBuilder teamFlagBuilder;
	@Autowired
	private Dao dao;
	@Autowired
	private Message message;

	public SendMessage topTeams(Integer count) {
		Document doc = getHtmlDocument(HLTV + "/ranking/teams");
		Elements header = doc.select("div.regional-ranking-header");
		Elements rankedTeams = doc.select("div.ranked-team");
		StringBuilder textMessage = new StringBuilder();
		textMessage.append(Emoji.MIL_MEDAL.getCode()).append("<b>").append(header.text()).append("</b>\n");
		for (Element team : rankedTeams) {
			if (team.select("span.position").text().equals("#" + (count + 1))) {
				break;
			}
			String teamProfileURL = team.select("div.more").select("a").attr("href");
			String teamFlag = teamFlagBuilder.getTeamProfileFlag(HLTV + teamProfileURL);
			StringBuilder row = new StringBuilder();
			row.append("<b>").append(team.select("span.position").text()).append("</b> (")
					.append(team.select("div.change").text()).append(") ").append(teamFlag)
					.append("<a href=\'https://hltv.org")
					.append(team.select("div.more").select("a[class=details moreLink]").attr("href")).append("\'>")
					.append(team.select("span.name").text()).append("</a> ").append(team.select("span.points").text())
					.append(" [");
			ArrayList<String> listPlayers = new ArrayList<>();
			for (Element player : team.select("div.rankingNicknames")) {
				listPlayers.add(player.text());
			}
			row.append(String.join(", ", listPlayers)).append("]\n");
			textMessage.append(row);
		}

		LOGGER.debug("TopTeams final message:\n{}", textMessage.toString());
		return message.createTextMessage(textMessage);
	}

	public SendMessage topPlayers(Integer count) {
		String year = String.valueOf(LocalDate.now().getYear());
		Document doc = getHtmlDocument(HLTV + "/stats/players?startDate=" + year + "-01-01&endDate=" + year + "-12-31");
		Elements rows = doc.select("tr");
		StringBuilder textMessage = new StringBuilder();
		textMessage.append(Emoji.SPORT_MEDAL.getCode()).append("<b>CS:GO World Top Players ").append(year)
				.append("</b>\n").append("<b>");
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
		LOGGER.debug("TopPlayers final message:\n{}", textMessage.toString());
		return message.createTextMessage(textMessage);
	}

	public SendMessage matches(Long chatId) {
		Document doc = getHtmlDocument(HLTV + "/matches");
		StringBuilder textMessage = new StringBuilder();
		if (doc.select("div.live-match").size() > 1) {
			textMessage.append("<b>Live matches</b>").append(Emoji.EXCL_MARK.getCode()).append("\n");
		}

		for (Element match : doc.select("div.live-match")) {
			if (match.text().isEmpty()) {
				continue;
			}

			textMessage.append(Emoji.CUP.getCode()).append("<a href=\'https://hltv.org")
					.append(match.select("a").attr("href")).append("\'>").append(match.select("div.event-name").text())
					.append("</a>\n").append(favoriteTeam(chatId, match.select("span.team-name").get(0).text(), false))
					.append(" ").append(Emoji.VS.getCode()).append(" ")
					.append(favoriteTeam(chatId, match.select("span.team-name").get(1).text(), false)).append(" (")
					.append(match.select("tr.header").select("td.bestof").text()).append(") ").append(getStars(match))
					.append("\n");

			Elements maps = match.select("tr.header").select("td.map");
			int numMaps = maps.size();

			for (int i = 0; i < numMaps; i++) {
				String first = match.select("td.livescore").select("span[data-livescore-map=" + (i + 1) + "]").get(0)
						.text();
				String second = match.select("td.livescore").select("span[data-livescore-map=" + (i + 1) + "]").get(1)
						.text();

				if (!(first.equals("-") || second.equals("-"))) {
					if (Integer.parseInt(first) > Integer.parseInt(second)) {
						first = "<b>" + first + "</b>";
					} else if (Integer.parseInt(first) < Integer.parseInt(second)) {
						second = "<b>" + second + "</b>";
					}
				}
				textMessage.append("<b>").append(maps.get(i).text()).append("</b>: ").append(first).append("-")
						.append(second).append("\n");
			}
			textMessage.append("\n");

		}

		textMessage.append("<b>Upcoming CS:GO matches\n");
		Element matchDay = doc.select("div.match-day").first();
		textMessage.append(matchDay.select("span.standard-headline").text()).append("</b>\n");

		for (Element match : matchDay.select("div.match")) {
			long unixTime = Long.parseLong(match.select("div.time").attr("data-unix"));
			LocalDateTime localTime = LocalDateTime.ofEpochSecond((unixTime / 1000) + 10800, 0, ZoneOffset.UTC);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
			String formattedTime = localTime.format(formatter);
			textMessage.append("<b>").append(formattedTime).append("</b> - ");

			if (!match.select("div.line-align").isEmpty()) {
				textMessage.append(favoriteTeam(chatId, match.select("td.team-cell").get(0).text(), true)).append(" ")
						.append(Emoji.VS.getCode()).append(" ")
						.append(favoriteTeam(chatId, match.select("td.team-cell").get(1).text(), true)).append(" (")
						.append(match.select("div.map-text").text()).append(") ").append(getStars(match))
						.append(Emoji.SQUARE.getCode()).append(" ").append("<a href=\'https://hltv.org")
						.append(match.select("a").attr("href")).append("\'>").append(match.select("td.event").text())
						.append("</a>\n");

			} else {
				textMessage.append(match.select("td.placeholder-text-cell").text()).append("\n");
			}
		}

		LOGGER.debug("Matches final message:\n{}", textMessage.toString());
		return message.createTextMessage(textMessage);

	}

	public SendMessage results(Long chatId) {
		Document doc = getHtmlDocument(HLTV + "/results");
		StringBuilder textMessage = new StringBuilder();
		Elements subLists = doc.select("div.results-sublist");
		for (Element resultList : subLists) {
			String headerText = resultList.select("span.standard-headline").text();
			if (headerText.isEmpty()) {
				headerText = "Featured Results";
			}

			textMessage.append(Emoji.CUP.getCode()).append(" <b>").append(headerText).append("</b>\n");

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
						.append(getStars(resultCon)).append(Emoji.SQUARE.getCode()).append(" ")
						.append("<a href=\'https://hltv.org").append(resultCon.select("a").attr("href")).append("\'>")
						.append(resultCon.select("td.event").text()).append("</a> \n");

			}
			textMessage.append("\n");

			if (headerText.startsWith("Results"))
				break;
		}

		LOGGER.debug("Results final message:\n{}", textMessage.toString());
		return message.createTextMessage(textMessage);
	}

	public Streams twitch(String uri) {
		String newUri = TWITCH;
		if (!StringUtils.isBlank(uri)) {
			newUri = newUri.concat("&after=" + uri);
		}
		JSONObject json = getJson(newUri);
		Streams streams = new Streams();
		String nextPageId = json.getJSONObject("pagination").getString("cursor");
		streams.setNextPageId(nextPageId);
		LOGGER.debug("Streams nextPageId: {}", nextPageId);
		StringBuilder textMessage = new StringBuilder();
		textMessage.append("<b>Live</b>").append(Emoji.EXCL_MARK.getCode()).append("<b>Streams on Twitch:</b>\n");
		JSONArray arr = json.getJSONArray("data");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject data = arr.getJSONObject(i);
			textMessage.append("<b>(").append(data.getNumber("viewer_count")).append(")</b> ")
					.append("<a href=\'https://www.twitch.tv/").append(data.getString("user_name")).append("\'>")
					.append(data.getString("user_name")).append("</a> ")
					.append(flagUnicodeFromCountry(data.getString("language").toUpperCase())).append(" ")
					.append(data.getString("title").replace("<", "").replace(">", "")).append("\n");
		}
		streams.setMessage(textMessage);
		LOGGER.debug("Streams final message:\n{}", textMessage.toString());
		return streams;

	}

//	public SendSticker createBotMessage(Integer uniqCount) {
//		int randomSize = BotMessages.getStickers().size() - 1 + 1;
//		int randomValue = new Random().nextInt(randomSize);
//		String selectedMessage = BotMessages.getStickers().get(randomValue);
//
//		while (BotMessages.getLastSticker().contains(selectedMessage)) {
//			randomValue = new Random().nextInt(randomSize);
//			selectedMessage = BotMessages.getStickers().get(randomValue);
//		}
//
//		if (BotMessages.getLastSticker().size() < uniqCount) {
//			BotMessages.getLastSticker().add(selectedMessage);
//		} else {
//			BotMessages.getLastSticker().remove(0);
//			BotMessages.getLastSticker().add(selectedMessage);
//		}
//		return new SendSticker().setSticker(selectedMessage);
//	}

	public SendMessage cite() {
		Document doc = getHtmlDocument(CITES);
		StringBuilder text = new StringBuilder();
		text.append(doc.select("cite").text());
		String athor = doc.select("small").text();
		if (!StringUtils.isEmpty(athor)) {
			text.append("\n<b>").append(athor).append("</b>");
		}

		return message.createTextMessage(text);
	}

	public SendMessage updateFavoriteTeam(Long chatId, String name, String countryCode) {
		DbResult dbResult = dao.updateOrSaveTeam(chatId, name, countryCode);
		switch (dbResult) {
		case FLAG_NOT_FOUND:
			return message.createTextMessage(DbResult.NOTHING_WAS_CHANGED.getText());
		case UPDATED:
			return message.createTextMessage("<b>" + name + "</b> " + DbResult.UPDATED.getText());
		case ALREADY_EXIST:
			return message.createTextMessage("<b>" + name + "</b> " + DbResult.ALREADY_EXIST.getText());
		case INSERTED:
			return message.createTextMessage("<b>" + name + "</b> " + DbResult.INSERTED.getText());
		default:
			return message.createTextMessage(DbResult.OOPS.getText());
		}
	}

	public SendMessage deleteTeam(Long chatId, String name) {
		DbResult dbResult = dao.deleteTeam(chatId, name);
		switch (dbResult) {
		case DELETED:
			return message.createTextMessage("<b>" + name + "</b> " + DbResult.DELETED.getText());
		case NOTHING_WAS_CHANGED:
			return message.createTextMessage(DbResult.NOTHING_WAS_CHANGED.getText());
		default:
			return message.createTextMessage(DbResult.OOPS.getText());
		}
	}

	public SendMessage getAllTeams(Long chatId) {
		StringBuilder textMessage = new StringBuilder();
		List<FavoriteTeam> teams = dao.getTeams().stream().filter(team -> team.getChatId().equals(chatId))
				.collect(Collectors.toList());
		if (teams.isEmpty())
			return message.createTextMessage(TEAMS_DESCRIPTION + "\n\n<b>У вас пока нет любимых команд!</b> "
					+ Emoji.SAD.getCode() + "\n\n" + TEAMS_COMMANDS);
		textMessage.append(TEAMS_DESCRIPTION).append("\nВаши любимые команды:\n\n");
		teams.stream()
				.forEach(team -> textMessage.append("<b>").append(team.getName()).append("</b> [")
						.append(team.getCountryCode().getCode()).append("] ")
						.append(flagUnicodeFromCountry(team.getCountryCode().getCode())).append("\n"));
		textMessage.append("\n").append(TEAMS_COMMANDS);
		return message.createTextMessage(textMessage);
	}

	public SendMessage teamsFormat() {
		return message.createTextMessage("Неверный формат!\nСмотрите примеры ниже!\n\n" + TEAMS_COMMANDS);
	}

	public SendMessage matchesForToday() {
		return new SendMessage().setText(MATCHES_FOR_TODAY);
	}

	public SendMessage resultsForToday() {
		return new SendMessage().setText(RESULTS_FOR_TODAY);
	}

	private StringBuilder getStars(Element match) {
		StringBuilder stars = new StringBuilder();
		match.select("div.stars").select("i").stream().forEach(star -> stars.append(Emoji.STAR.getCode()));
		return stars;
	}

	@Cacheable
	private String getHttpResponse(String uri) {
		try {
			GetMethod get = new GetMethod(uri);
			get.setFollowRedirects(true);
			get.setRequestHeader(HttpHeaders.USER_AGENT, USER_AGENT_NAME);
			get.setRequestHeader(CLIENT_ID, clientId);
			CLIENT.executeMethod(get);
			String response = get.getResponseBodyAsString();
			get.releaseConnection();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error(EXCEPTION_MSG);
		}
	}

	protected Document getHtmlDocument(String uri) {
		return Jsoup.parse(getHttpResponse(uri));
	}

	private JSONObject getJson(String uri) {
		return new JSONObject(getHttpResponse(uri));
	}

	private String favoriteTeam(Long chatId, String name, boolean isBold) {
		String teamName = name;
		FavoriteTeam fvTeam = dao.getTeams().parallelStream()
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
		LOGGER.debug("Country code is: {}", country);
		String text = null;
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
		}
		if (text == null)
			text = EmojiParser.parseToUnicode(":un:");
		return text;
	}
	
}
