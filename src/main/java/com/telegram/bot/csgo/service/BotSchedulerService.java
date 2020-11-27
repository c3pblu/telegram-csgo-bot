package com.telegram.bot.csgo.service;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.hltv.MatchesService;
import com.telegram.bot.csgo.service.hltv.ResultsService;
import com.telegram.bot.csgo.service.http.HttpService;

@Service
public class BotSchedulerService {

	private final static String HLTV = "https://www.hltv.org";

	@Value(value = "${bot.scheduler.chat.id}")
	private String schedulerChatId;

	private BotController botController;
	private HttpService httpService;
	private MatchesService matchesService;
	private ResultsService resultsService;

	@Autowired
	public BotSchedulerService(BotController botController, HttpService httpService, MatchesService matchesService,
			ResultsService resultsService) {
		this.botController = botController;
		this.httpService = httpService;
		this.matchesService = matchesService;
		this.resultsService = resultsService;
	}

	@Scheduled(cron = "${bot.scheduler.matches.cron}")
	private void todayMatchesScheduler() {
		botController.sendMessage(matchesService.matchesForToday(schedulerChatId));
		Document doc = httpService.getDocument(HLTV + "/matches");
		botController.sendMessage(matchesService.matches(schedulerChatId, doc));
	}

	@Scheduled(cron = "${bot.scheduler.results.cron}")
	private void todayResultsScheduler() {
		botController.sendMessage(resultsService.resultsForToday(schedulerChatId));
		Document doc = httpService.getDocument(HLTV + "/results");
		botController.sendMessage(resultsService.results(schedulerChatId, doc));
	}

}
