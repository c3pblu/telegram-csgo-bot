package com.telegram.bot.csgo.service;

import static com.telegram.bot.csgo.helper.HtmlTagsHelper.TEAMS_COUNTRY;
import static com.telegram.bot.csgo.helper.MessageHelper.HLTV_URL;
import com.telegram.bot.csgo.service.http.HttpService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamCountryService {

    private final HttpService httpService;

    @Cacheable("teamCountry")
    public String getCountry(String url) {
        return httpService.getAsDocument(HLTV_URL + url)
                .select(TEAMS_COUNTRY)
                .text();
    }
}
