package com.telegram.bot.csgo.helper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.model.FavoriteTeams;
import com.vdurmont.emoji.EmojiParser;

public final class MessageHelper {

	private final static String USER_AGENT_NAME = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
	private final static String HLTV = "https://www.hltv.org";
	private final static String HTML = "html";
	private final static String EXCEPTION_MSG = "Не смог получить данные с сайта...";

	private MessageHelper() {

	}

	public static SendMessage topTeams(Integer count) {
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(HLTV + "/ranking/teams");
		get.setFollowRedirects(true);
		get.setRequestHeader(HttpHeaders.USER_AGENT, USER_AGENT_NAME);
		SendMessage sendMessage = new SendMessage();
		sendMessage.setParseMode(HTML);
		try {
			client.executeMethod(get);
			String response = get.getResponseBodyAsString();
			get.releaseConnection();
			Document doc = Jsoup.parse(response);
			Elements header = doc.select("div.regional-ranking-header");
			Elements rankedTeams = doc.select("div.ranked-team");
			StringBuilder textMessage = new StringBuilder();
			textMessage.append("<b>").append(header.text()).append("</b>\n\n");
			for (Element team : rankedTeams) {
				if (team.select("span.position").text().equals("#" + (count + 1))) {
					break;
				}
				StringBuilder row = new StringBuilder();
				row.append("<a href=\'").append(team.select("div.ranking-header").select("img").attr("src"))
						.append("\'></a>").append("<b>").append(team.select("span.position").text()).append("</b> (")
						.append(team.select("div.change").text()).append(") ").append("<a href=\'https://hltv.org")
						.append(team.select("div.more").select("a[class=details moreLink]").attr("href")).append("\'>")
						.append(team.select("span.name").text()).append("</a> ")
						.append(team.select("span.points").text()).append(" [");
				ArrayList<String> listPlayers = new ArrayList<>();
				for (Element player : team.select("div.rankingNicknames")) {
					listPlayers.add(player.text());
				}
				row.append(String.join(", ", listPlayers)).append("]\n");
				textMessage.append(row);
			}
			sendMessage.setText(textMessage.toString());

		} catch (IOException e) {
			e.printStackTrace();
			sendMessage.setText(EXCEPTION_MSG);
		}
		return sendMessage;
	}

	public static SendMessage topPlayers(Integer count) {
		HttpClient client = new HttpClient();
		String year = String.valueOf(LocalDate.now().getYear());
		GetMethod get = new GetMethod(HLTV + "/stats/players?startDate=" + year + "-01-01&endDate=" + year + "-12-31");
		get.setFollowRedirects(true);
		get.setRequestHeader(HttpHeaders.USER_AGENT, USER_AGENT_NAME);
		SendMessage sendMessage = new SendMessage();
		sendMessage.setParseMode(HTML);
		try {
			client.executeMethod(get);
			String response = get.getResponseBodyAsString();
			get.releaseConnection();
			Document doc = Jsoup.parse(response);
			Elements rows = doc.select("tr");
			StringBuilder textMessage = new StringBuilder();
			textMessage.append("<b>CS:GO World Top Players ").append(year).append("</b>\n\n").append("<b>")
					.append(doc.select("tr.stats-table-row").text()).append("</b>\n");
			int number = 1;
			for (Element value : rows) {
				if (value.select("td.statsDetail").first() == null) {
					continue;
				}
				if (number > count) {
					break;
				}
				textMessage.append("<b>#").append(number).append("</b> ").append("<a href=\'https://hltv.org")
						.append(value.select("td.playerCol").select("a").attr("href")).append("\'>")
						.append(value.select("td.playerCol").text()).append("</a>, ")
						.append(value.select("td.teamCol").select("img").attr("title")).append(", ")
						.append(value.select("td.statsDetail").get(0).text()).append(", ")
						.append(value.select("td.kdDiffCol").text()).append(", ")
						.append(value.select("td.statsDetail").get(1).text()).append(", ")
						.append(value.select("td.ratingCol").text()).append("\n");
				number++;
			}
			sendMessage.setText(textMessage.toString());

		} catch (IOException e) {
			e.printStackTrace();
			sendMessage.setText(EXCEPTION_MSG);

		}
		return sendMessage;
	}

	public static SendMessage matches() {
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(HLTV + "/matches");
		get.setFollowRedirects(true);
		get.setRequestHeader(HttpHeaders.USER_AGENT, USER_AGENT_NAME);
		SendMessage sendMessage = new SendMessage();
		sendMessage.setParseMode(HTML);
		try {
			client.executeMethod(get);
			String response = get.getResponseBodyAsString();
			get.releaseConnection();
			Document doc = Jsoup.parse(response);
			StringBuilder textMessage = new StringBuilder();
			if (doc.select("div.live-match").size() > 1) {
				textMessage.append("<b>Live matches</b>\u2757\n");
			}

			for (Element match : doc.select("div.live-match")) {
				if (match.text().isEmpty()) {
					continue;
				}

				textMessage.append("\uD83C\uDFC6 <b>").append(match.select("div.event-name").text()).append("\n")
						.append("</b>").append(unlinkName(match.select("span.team-name").get(0).text()))
						.append(" \uD83C\uDD9A ").append(unlinkName(match.select("span.team-name").get(1).text()))
						.append(" (").append(match.select("tr.header").select("td.bestof").text()).append(")\n");

				Elements maps = match.select("tr.header").select("td.map");
				int numMaps = maps.size();

				for (int i = 0; i < numMaps; i++) {
					StringBuilder mapsString = new StringBuilder();
					String first = match.select("td.livescore").select("span[data-livescore-map=" + (i + 1) + "]")
							.get(0).text();
					String second = match.select("td.livescore").select("span[data-livescore-map=" + (i + 1) + "]")
							.get(1).text();

					if (!(first.equals("-") && second.equals("-"))) {
						if (Integer.parseInt(first) > Integer.parseInt(second)) {
							first = "<b>" + first + "</b>";
						} else if (Integer.parseInt(first) < Integer.parseInt(second)) {
							second = "<b>" + second + "</b>";
						}
					}

					mapsString.append("<b>").append(maps.get(i).text()).append("</b>: ").append(first).append("-")
							.append(second).append("\n");
					textMessage.append(mapsString);
				}

				textMessage.append("\n");

			}

			textMessage.append("<b>Upcoming CS:GO matches\n");
			Element matchDay = doc.select("div.match-day").first();
			textMessage.append(matchDay.select("span.standard-headline").text()).append("</b>\n");

			for (Element match : matchDay.select("table.table")) {
				long unixTime = Long.parseLong(match.select("div.time").attr("data-unix"));
				LocalDateTime localTime = LocalDateTime.ofEpochSecond((unixTime / 1000) + 10800, 0, ZoneOffset.UTC);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
				String formattedTime = localTime.format(formatter);
				textMessage.append(formattedTime).append(" - ");

				if (!match.select("div.line-align").isEmpty()) {
					textMessage.append(favoriteTeam(match.select("td.team-cell").get(0).text(), false)).append(" vs ")
							.append(favoriteTeam(match.select("td.team-cell").get(1).text(), false)).append(" (")
							.append(match.select("div.map-text").text()).append(") ");

					for (int i = 0; i < match.select("div.stars").select("i").size(); i++) {
						textMessage.append(EmojiParser.parseToUnicode(":star:"));
					}

					textMessage.append(" \u25AB ").append(match.select("td.event").text()).append("\n");

				} else {
					textMessage.append(match.select("td.placeholder-text-cell").text()).append("\n");
				}
			}

			sendMessage.setText(textMessage.toString());

		} catch (IOException e) {
			e.printStackTrace();
			sendMessage.setText(EXCEPTION_MSG);
		}

		return sendMessage;

	}

	public static SendMessage results() {
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(HLTV + "/results");
		get.setFollowRedirects(true);
		get.setRequestHeader(HttpHeaders.USER_AGENT, USER_AGENT_NAME);
		SendMessage sendMessage = new SendMessage();
		sendMessage.setParseMode(HTML);
		try {
			client.executeMethod(get);
			String response = get.getResponseBodyAsString();
			get.releaseConnection();
			Document doc = Jsoup.parse(response);
			StringBuilder textMessage = new StringBuilder();
			Elements subLists = doc.select("div.results-sublist");
			int i = 0;
			for (Element resultList : subLists) {
				if (i > 1)
					break;
				String headerText = resultList.select("span.standard-headline").text();
				if (headerText.isEmpty()) {
					headerText = "Featured Results";
				}

				textMessage.append("\uD83C\uDFC6 <b>").append(headerText).append("</b>\n");

				for (Element resultCon : resultList.select("div.result-con")) {
					Element team1 = resultCon.select("div.team").get(0);
					Element team2 = resultCon.select("div.team").get(1);
					String team1String = favoriteTeam(resultCon.select("div.team").get(0).text(), true);
					String team2String = favoriteTeam(resultCon.select("div.team").get(1).text(), true);

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

					textMessage.append(" (").append(resultCon.select("div.map-text").text()).append(") \u25AB ")
							.append(resultCon.select("td.event").text());

					textMessage.append("\n");

				}
				textMessage.append("\n");
				i++;
			}

			sendMessage.setText(textMessage.toString());

		} catch (IOException e) {

		}
		return sendMessage;
	}

	private static String unlinkName(String name) {
		if (name.contains(".")) {
			name = name.replace('.', ',');
		}

		return name;
	}

	private static String favoriteTeam(String name, boolean isResult) {
		name = unlinkName(name);
		if (FavoriteTeams.isFavorite(name)) {
			if (isResult == true) {
				name = FavoriteTeams.getFlag(name) + name;
			} else {
				name = FavoriteTeams.getFlag(name) + "<b>" + name + "</b>";
			}
		}
		return name;
	}
}
