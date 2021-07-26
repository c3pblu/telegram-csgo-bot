package com.telegram.bot.csgo.adaptor;

import java.util.ArrayList;
import java.util.List;

import com.telegram.bot.csgo.repository.EmojiRepository;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.model.HtmlMessage;
import com.telegram.bot.csgo.service.HttpService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TopTeamsAdaptor {

    private final HttpService httpService;
    private final FlagsAdaptor flagsAdaptor;
    private final TopTeamsAdaptor self;
    private final EmojiRepository emojiRepository;

    @Autowired
    public TopTeamsAdaptor(HttpService httpService, FlagsAdaptor flagsAdaptor, @Lazy TopTeamsAdaptor self, EmojiRepository emojiRepository) {
        this.httpService = httpService;
        this.flagsAdaptor = flagsAdaptor;
        this.self = self;
        this.emojiRepository = emojiRepository;
    }

    public SendMessage topTeams(String chatId, Document doc, Integer count) {
        StringBuilder textMessage = new StringBuilder();
        textMessage
                .append(emojiRepository.getEmoji("mil_medal"))
                .append("<b>")
                .append(doc.select("div.regional-ranking-header").text())
                .append("</b>\n");
        for (Element team : doc.select("div.ranked-team")) {
            if (team.select("span.position").text().equals("#" + (count + 1))) {
                break;
            }
            String teamProfileURL = team.select("div.more").select("a").attr("href");
            log.debug("Team profile URL: {}", teamProfileURL);
            String country = self.getCountry(teamProfileURL);
            String teamFlag = flagsAdaptor.flagUnicodeFromCountry(country);
            StringBuilder row = new StringBuilder();
            row.append("<b>").append(team.select("span.position").text()).append("</b> (")
                    .append(team.select("div.change").text()).append(") ").append(teamFlag)
                    .append("<a href='https://hltv.org")
                    .append(team.select("div.more").select("a[class=details moreLink]").attr("href")).append("'>")
                    .append(team.select("span.name").text()).append("</a> ").append(team.select("span.points").text())
                    .append(" [");
            List<String> listPlayers = new ArrayList<>();
            for (Element player : team.select("div.rankingNicknames")) {
                listPlayers.add(player.text());
            }
            row.append(String.join(", ", listPlayers)).append("]\n");
            textMessage.append(row);
        }

        log.debug("TopTeams final message:\n{}", textMessage);
        return new HtmlMessage(chatId, textMessage);
    }

    @Cacheable("teamCountry")
    public String getCountry(String url) {
        String country = httpService.getDocument(HttpService.HLTV + url).select("div.team-country").text();
        System.gc();
        return country;
    }

}
