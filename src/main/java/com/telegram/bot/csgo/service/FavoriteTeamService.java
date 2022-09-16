package com.telegram.bot.csgo.service;

import java.util.List;
import java.util.Optional;
import static com.telegram.bot.csgo.helper.MessageHelper.BOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.COMMA;
import static com.telegram.bot.csgo.helper.MessageHelper.DOT;
import static com.telegram.bot.csgo.helper.MessageHelper.UNBOLD;
import com.telegram.bot.csgo.domain.FavoriteTeam;
import com.telegram.bot.csgo.repository.FavoriteTeamRepo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteTeamService {

    private final FlagService flagService;
    private final FavoriteTeamRepo favoriteTeamRepo;

    public List<FavoriteTeam> getAllByChatId(String chatId) {
        return favoriteTeamRepo.findByChatId(chatId);
    }

    public Optional<FavoriteTeam> getOneByChatIdAndName(String chatId, String name) {
        return favoriteTeamRepo.findByChatIdAndName(chatId, name);
    }

    @Transactional
    public void save(@NonNull FavoriteTeam favoriteTeam) {
        favoriteTeamRepo.save(favoriteTeam);
    }

    @Transactional
    public void delete(@NonNull FavoriteTeam favoriteTeam) {
        favoriteTeamRepo.delete(favoriteTeam);
    }

    public String highlightFavoriteTeam(String chatId, String name, boolean isBold) {
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

    private static String unlinkName(String name) {
        return name.contains(DOT.toString())
                ? name.replace(DOT, COMMA)
                : name;
    }
}
