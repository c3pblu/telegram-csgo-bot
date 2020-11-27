package com.telegram.bot.csgo.service.hltv;

import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class HltvService {

	private ResultsService resultsService;
	private MatchesService matchesService;
	private TopPlayersService topPlayersService;
	private TopTeamsService topTeamsService;
	private ScoreBotService scoreBotService;

	@Autowired
	public HltvService(ResultsService resultsService, MatchesService matchesService,
			TopPlayersService topPlayersService, TopTeamsService topTeamsService, ScoreBotService scoreBotService) {
		this.resultsService = resultsService;
		this.matchesService = matchesService;
		this.topPlayersService = topPlayersService;
		this.topTeamsService = topTeamsService;
		this.scoreBotService = scoreBotService;
	}

	public SendMessage resultsMessage(String chatId, Document doc) {
		return resultsService.results(chatId, doc);
	}

	public SendMessage matchesMessage(String chatId, Document doc) {
		return matchesService.matches(chatId, doc);
	}

	public SendMessage topPlayersMessage(String chatId, Document doc, Integer count) {
		return topPlayersService.topPlayers(chatId, doc, count);
	}

	public SendMessage topTeamsMessage(String chatId, Document doc, Integer count) {
		return topTeamsService.topTeams(chatId, doc, count);

	}

	public SendMessage scorebotLiveMessage(String chatId, JSONObject json) {
		return scoreBotService.scorebot(chatId, json);
	}

	public void startBroadcast(String chatId, String matchId) {
		scoreBotService.live(chatId, matchId);
	}

	public void stopBroadcast(String chatId) {
		scoreBotService.stop(chatId);
	}

	public SendMessage scorebotHelpMessage(String chatId) {
		return scoreBotService.scorebotHelp(chatId);
	}

}
