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

import com.telegram.bot.csgo.model.Constants;
import com.telegram.bot.csgo.model.FavoriteTeams;
import com.vdurmont.emoji.EmojiParser;

public final class MessageHelper {

	private static final HttpClient client = new HttpClient();

	private MessageHelper() {

	}

	public static SendMessage topTeams(Integer count) {
		Document doc = getDocument("/ranking/teams");
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
					.append(team.select("span.name").text()).append("</a> ").append(team.select("span.points").text())
					.append(" [");
			ArrayList<String> listPlayers = new ArrayList<>();
			for (Element player : team.select("div.rankingNicknames")) {
				listPlayers.add(player.text());
			}
			row.append(String.join(", ", listPlayers)).append("]\n");
			textMessage.append(row);
		}

		return new SendMessage().setParseMode(Constants.HTML).setText(textMessage.toString());
	}

	public static SendMessage topPlayers(Integer count) {
		String year = String.valueOf(LocalDate.now().getYear());
		Document doc = getDocument("/stats/players?startDate=" + year + "-01-01&endDate=" + year + "-12-31");
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
		return new SendMessage().setParseMode(Constants.HTML).setText(textMessage.toString());
	}

	public static SendMessage matches() {
		Document doc = getDocument("/matches");
		StringBuilder textMessage = new StringBuilder();
		if (doc.select("div.live-match").size() > 1) {
			textMessage.append("<b>Live matches</b>").append(Constants.EMOJI_EXCL_MARK).append("\n");
		}

		for (Element match : doc.select("div.live-match")) {
			if (match.text().isEmpty()) {
				continue;
			}

			textMessage.append(Constants.EMOJI_CUP).append(" <b>").append(match.select("div.event-name").text())
					.append("\n").append("</b>").append(unlinkName(match.select("span.team-name").get(0).text()))
					.append(" ").append(Constants.EMOJI_VS).append(" ")
					.append(unlinkName(match.select("span.team-name").get(1).text())).append(" (")
					.append(match.select("tr.header").select("td.bestof").text()).append(") ").append(getStars(match))
					.append("\n");

			Elements maps = match.select("tr.header").select("td.map");
			int numMaps = maps.size();

			for (int i = 0; i < numMaps; i++) {
				StringBuilder mapsString = new StringBuilder();
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
						.append(match.select("div.map-text").text()).append(") ").append(getStars(match))
						.append(Constants.EMOJI_SQUARE).append(" ").append(match.select("td.event").text())
						.append("\n");

			} else {
				textMessage.append(match.select("td.placeholder-text-cell").text()).append("\n");
			}
		}

		return new SendMessage().setParseMode(Constants.HTML).setText(textMessage.toString());

	}

	public static SendMessage results() {
		Document doc = getDocument("/results");
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

			textMessage.append(Constants.EMOJI_CUP).append(" <b>").append(headerText).append("</b>\n");

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

				textMessage.append(" (").append(resultCon.select("div.map-text").text()).append(") ")
						.append(getStars(resultCon)).append(Constants.EMOJI_SQUARE).append(" ")
						.append(resultCon.select("td.event").text()).append("\n");

			}
			textMessage.append("\n");
			i++;
		}

		return new SendMessage().setParseMode(Constants.HTML).setText(textMessage.toString());
	}

	public static SendMessage matchesForToday() {
		return new SendMessage().setText(Constants.MATCHES_FOR_TODAY);
	}

	private static StringBuilder getStars(Element match) {
		StringBuilder stars = new StringBuilder();
		for (int j = 0; j < match.select("div.stars").select("i").size(); j++) {
			stars.append(EmojiParser.parseToUnicode(":star:"));
		}
		return stars;

	}

	private static Document getDocument(String uri) {
		try {
			GetMethod get = new GetMethod(Constants.HLTV + uri);
			get.setFollowRedirects(true);
			get.setRequestHeader(HttpHeaders.USER_AGENT, Constants.USER_AGENT_NAME);
			client.executeMethod(get);
			String response = get.getResponseBodyAsString();
			get.releaseConnection();
			Document doc = Jsoup.parse(response);
			return doc;
		} catch (IOException e) {
			e.printStackTrace();
			throw new Error(Constants.EXCEPTION_MSG);
		}
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
