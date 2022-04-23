package com.telegram.bot.csgo.service;

import static com.telegram.bot.csgo.helper.CommandHelper.MATCHES_COMMAND;
import static com.telegram.bot.csgo.helper.CommandHelper.RESULTS_COMMAND;
import static com.telegram.bot.csgo.helper.MessageHelper.HLTV_URL;
import com.telegram.bot.csgo.adaptor.MatchesAdaptor;
import com.telegram.bot.csgo.adaptor.ResultsAdaptor;
import com.telegram.bot.csgo.config.properties.BotProperties;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.model.message.HtmlMessage;
import com.telegram.bot.csgo.service.http.HttpService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final BotController botController;
    private final HttpService httpService;
    private final MatchesAdaptor matchesAdaptor;
    private final ResultsAdaptor resultsAdaptor;
    private final BotProperties botProperties;

    private static final String UPCOMING_MATCHES_MSG = "Upcoming matches:";
    private static final String RESULTS_MSG = "Last results:";

    @Scheduled(cron = "${bot.scheduler.matches-cron}")
    private void todayMatchesScheduler() {
        var chatId = botProperties.getScheduler().getChatId();
        var doc = httpService.getAsDocument(HLTV_URL + MATCHES_COMMAND);
        botController.send(new HtmlMessage(chatId, UPCOMING_MATCHES_MSG));
        botController.send(matchesAdaptor.matches(chatId, doc));
    }

    @Scheduled(cron = "${bot.scheduler.results-cron}")
    private void todayResultsScheduler() {
        var chatId = botProperties.getScheduler().getChatId();
        var doc = httpService.getAsDocument(HLTV_URL + RESULTS_COMMAND);
        botController.send(new HtmlMessage(chatId, RESULTS_MSG));
        botController.send(resultsAdaptor.results(chatId, doc));
    }
}
