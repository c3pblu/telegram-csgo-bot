package com.telegram.bot.csgo.service;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.telegram.bot.csgo.adaptor.MatchesAdaptor;
import com.telegram.bot.csgo.adaptor.ResultsAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.model.HtmlMessage;

@Service
public class SchedulerService {

	@Value("${bot.scheduler.chat.id}")
	private String schedulerChatId;

	private BotController botController;
	private HttpService httpService;
	private MatchesAdaptor matchesAdaptor;
	private ResultsAdaptor resultsAdaptor;

	@Autowired
	public SchedulerService(BotController botController, HttpService httpService, MatchesAdaptor matchesAdaptor,
			ResultsAdaptor resultsAdaptor) {
		this.botController = botController;
		this.httpService = httpService;
		this.matchesAdaptor = matchesAdaptor;
		this.resultsAdaptor = resultsAdaptor;
	}

	@Scheduled(cron = "${bot.scheduler.matches.cron}")
	private void todayMatchesScheduler() {
		Document doc = httpService.getDocument(HttpService.HLTV + "/matches");
		botController.send(new HtmlMessage(schedulerChatId, "Ближайшие матчи:"));
		botController.send(matchesAdaptor.matches(schedulerChatId, doc));
	}

	@Scheduled(cron = "${bot.scheduler.results.cron}")
	private void todayResultsScheduler() {
		Document doc = httpService.getDocument(HttpService.HLTV + "/results");
		botController.send(new HtmlMessage(schedulerChatId, "Последние результаты:"));
		botController.send(resultsAdaptor.results(schedulerChatId, doc));
	}

}
