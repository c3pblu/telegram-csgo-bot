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

	public SendMessage resultsMessage(Document doc, Long chatId) {
		return resultsService.results(doc, chatId);
	}

	public SendMessage matchesMessage(Document doc, Long chatId) {
		return matchesService.matches(doc, chatId);
	}

	public SendMessage topPlayersMessage(Document doc, Integer count) {
		return topPlayersService.topPlayers(doc, count);
	}

	public SendMessage topTeamsMessage(Document doc, Integer count) {
		return topTeamsService.topTeams(doc, count);

	}

	public SendMessage scorebotLiveMessage(JSONObject json) {
		return scoreBotService.scorebot(json);
	}

	public void startBroadcast(Long chatId, String matchId) {
		scoreBotService.live(chatId, matchId);
	}

	public void stopBroadcast(Long chatId) {
		scoreBotService.stop(chatId);
	}
	
	public SendMessage scorebotHelpMessage() {
		return scoreBotService.scorebotHelp();
	}

}
