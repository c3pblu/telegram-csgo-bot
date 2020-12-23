package com.telegram.bot.csgo.adaptor;

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

@Component
public class ResultsAdaptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResultsAdaptor.class);

	private FlagsAdaptor flagsAdaptor;

	@Autowired
	public ResultsAdaptor(FlagsAdaptor flagsAdaptor) {
		this.flagsAdaptor = flagsAdaptor;
	}

	public SendMessage results(String chatId, Document doc) {
		StringBuilder textMessage = new StringBuilder();
		Elements subLists = doc.select("div.results-sublist");
		int featuredNum = 0;
		for (Element resultList : subLists) {
			String headerText = resultList.select("span.standard-headline").text();
			if (headerText.isEmpty()) {
				headerText = doc.select("div.tab-holder").select("div.tab").get(featuredNum).text();
				featuredNum++;
			}

			textMessage.append(Emoji.CUP).append(" <b>").append(headerText).append("</b>\n");

			for (Element resultCon : resultList.select("div.result-con")) {
				Element team1 = resultCon.select("div.team").get(0);
				Element team2 = resultCon.select("div.team").get(1);
				String team1String = flagsAdaptor.favoriteTeam(chatId, resultCon.select("div.team").get(0).text(),
						false);
				String team2String = flagsAdaptor.favoriteTeam(chatId, resultCon.select("div.team").get(1).text(),
						false);

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
		return new HtmlMessage(chatId, textMessage);
	}

	private StringBuilder getStars(Element match) {
		StringBuilder stars = new StringBuilder();
		match.select("div.stars").select("i").stream().forEach(star -> stars.append(Emoji.STAR));
		return stars;
	}

}
