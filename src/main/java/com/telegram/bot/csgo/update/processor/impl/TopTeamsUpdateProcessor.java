package com.telegram.bot.csgo.update.processor.impl;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.adaptor.TopTeamsAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.HttpService;
import com.telegram.bot.csgo.update.processor.UpdateProcessor;

@Component
public class TopTeamsUpdateProcessor implements UpdateProcessor {

    private BotController botController;
    private HttpService httpService;
    private TopTeamsAdaptor topTeamsAdaptor;

    @Autowired
    public TopTeamsUpdateProcessor(BotController botController, HttpService httpService,
            TopTeamsAdaptor topTeamsAdaptor) {
        this.botController = botController;
        this.httpService = httpService;
        this.topTeamsAdaptor = topTeamsAdaptor;
    }

    private static final String TOP_10_COMMAND = ".топ10";
    private static final String TOP_20_COMMAND = ".топ20";
    private static final String TOP_30_COMMAND = ".топ30";

    @Override
    public void process(Update update) {
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            if (TOP_10_COMMAND.equalsIgnoreCase(text) || TOP_20_COMMAND.equalsIgnoreCase(text)
                    || TOP_30_COMMAND.equalsIgnoreCase(text)) {
                Integer count = Integer.parseInt(update.getMessage().getText().substring(4));
                topTeams(getChatId(update), count);
            }
        }
        if ((update.hasCallbackQuery())) {
            String data = update.getCallbackQuery().getData();
            if (data != null && data.matches("top[\\d][\\d]")) {
                topTeams(getChatId(update), Integer.parseInt(data.replaceAll("\\D", "")));
                deleteMenu(botController, update);
            }
        }
    }

    private void topTeams(String chatId, int count) {
        Document doc = httpService.getDocument(HttpService.HLTV + "/ranking/teams");
        botController.send(topTeamsAdaptor.topTeams(chatId, doc, count));
    }
}
