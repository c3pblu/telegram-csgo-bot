package com.telegram.bot.csgo.service;

import static com.telegram.bot.csgo.helper.MessageHelper.BOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.COMMA;
import static com.telegram.bot.csgo.helper.MessageHelper.DOT;
import static com.telegram.bot.csgo.helper.MessageHelper.UNBOLD;
import com.telegram.bot.csgo.repository.FavoriteTeamRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteTeamService {

    private final FlagService flagService;
    private final FavoriteTeamRepo favoriteTeamRepo;

    public String favoriteTeam(String chatId, String name, boolean isBold) {
        var teamName = name;
        var fvTeam = favoriteTeamRepo.findByChatIdAndName(chatId, name);
        if (fvTeam.isPresent()) {
            var flag = flagService.flagUnicodeFromCountry(fvTeam.get().getFlag().getCode());
            teamName = isBold
                    ? (flag + BOLD + name + UNBOLD)
                    : (flag + name);
        }
        return unlinkName(teamName);
    }

    private String unlinkName(String name) {
        return name.contains(DOT.toString())
                ? name.replace(DOT, COMMA)
                : name;
    }
}
