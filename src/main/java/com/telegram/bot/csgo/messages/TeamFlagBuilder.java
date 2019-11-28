package com.telegram.bot.csgo.messages;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class TeamFlagBuilder {

    @Autowired
    private MessageBuilder messageBuilder;

    @Cacheable("teamsFlags")
    public String getTeamProfileFlag(String url) {
        Document teamProfile = messageBuilder.getHtmlDocument(url);
        return messageBuilder.flagUnicodeFromCountry(teamProfile.select("div.team-country").text().trim());
    }

}
