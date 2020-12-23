package com.telegram.bot.csgo.adaptor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.model.HtmlMessage;
import com.telegram.bot.csgo.service.HttpService;

@Component
public class MatchesAdaptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MatchesAdaptor.class);

	private FlagsAdaptor flagsAdaptor;
	private HttpService httpService;

	@Autowired
	public MatchesAdaptor(HttpService httpService, FlagsAdaptor flagsAdaptor) {
		this.httpService = httpService;
		this.flagsAdaptor = flagsAdaptor;

	}

	public SendMessage matches(String chatId, Document doc) {
		StringBuilder textMessage = new StringBuilder();
		// Live Matches
		Elements liveMatches = doc.select("div.liveMatchesContainer").select("div.liveMatches");
		if (!liveMatches.isEmpty()) {
			textMessage.append("<b>Live matches</b>").append(Emoji.EXCL_MARK).append("\n");
			for (Element match : liveMatches.select("div.liveMatch")) {
				textMessage.append(Emoji.CUP).append("<a href=\'https://hltv.org")
						.append(match.select("a").attr("href")).append("\'>")
						.append(match.select("div.matchEventName").text()).append("</a>\n")
						.append(flagsAdaptor
								.favoriteTeam(chatId, match.select("div.matchTeamName").get(0).text(), true))
						.append(" ").append(Emoji.VS).append(" ")
						.append(flagsAdaptor.favoriteTeam(chatId, match.select("div.matchTeamName").get(1).text(),
								true))
						.append(" (").append(match.select("div.matchMeta").text()).append(") ").append(getStars(match))
						.append("\nMatch ID: ").append(match.select("div.liveMatch").attr("data-livescore-match"))
						.append("\n");
				Document matchPage = httpService.getDocument("https://hltv.org" + match.select("a").attr("href"));
				for (Element map : matchPage.select("div.mapholder")) {
					String team1Score = map.select("div.results-team-score").get(0).text();
					String team2Score = map.select("div.results-team-score").get(1).text();
					// Is first team won?
					if (!map.select("div.results-left").get(0).select("div.won").isEmpty()) {
						team1Score = "<b>" + team1Score + "</b>";
					}
					// Is first team lost?
					if (!map.select("div.results-left").get(0).select("div.lost").isEmpty()) {
						team2Score = "<b>" + team2Score + "</b>";
					}
					textMessage.append(map.select("div.mapname").text()).append(": ").append(team1Score).append("-")
							.append(team2Score).append("\n");
				}
				textMessage.append("\n");

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
				textMessage.append(flagsAdaptor.favoriteTeam(chatId, match.select("div.matchTeam.team1").text(), true))
						.append(" ").append(Emoji.VS).append(" ")
						.append(flagsAdaptor.favoriteTeam(chatId, match.select("div.matchTeam.team2").text(), true))
						.append(" (").append(match.select("div.matchMeta").text()).append(") ");
			}

			textMessage.append(getStars(match)).append(Emoji.SQUARE).append(" ").append("<a href=\'https://hltv.org")
					.append(match.select("a").attr("href")).append("\'>")
					.append(match.select("div.matchEventName").text()).append("</a>\n");

		}

		LOGGER.debug("Matches final message:\n{}", textMessage);
		return new HtmlMessage(chatId, textMessage);

	}

	private StringBuilder getStars(Element match) {
		StringBuilder stars = new StringBuilder();
		for (int i = 0; i < 5 - match.select("i.fa.fa-star.faded").size(); i++) {
			stars.append(Emoji.STAR);
		}
		return stars;
	}

}
