package com.telegram.bot.csgo.service.hltv;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.service.http.HttpService;
import com.telegram.bot.csgo.service.message.MessageService;

@Service
public class TopTeamsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TopTeamsService.class);

	private HttpService httpService;
	private MessageService messageService;

	@Autowired
	public TopTeamsService(HttpService httpService, MessageService messageService) {
		this.httpService = httpService;
		this.messageService = messageService;

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
			String teamFlag = messageService
					.flagUnicodeFromCountry(teamProfile.select("div.team-country").text().trim());

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
		return messageService.htmlMessage(textMessage);
	}

}
