package com.telegram.bot.csgo.service.hltv;

import java.time.LocalDate;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.service.message.MessageService;

@Service
public class TopPlayersService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TopPlayersService.class);

	private MessageService messageService;

	@Autowired
	public TopPlayersService(MessageService messageService) {
		this.messageService = messageService;
	}

	public SendMessage topPlayers(String chatId, Document doc, Integer count) {
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
					.append(messageService
							.flagUnicodeFromCountry(value.select("td.playerCol").select("img").attr("title")))
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
		return messageService.htmlMessage(chatId, textMessage);
	}

}
