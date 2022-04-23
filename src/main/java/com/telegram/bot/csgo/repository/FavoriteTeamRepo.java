package com.telegram.bot.csgo.repository;

import com.telegram.bot.csgo.domain.FavoriteTeam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteTeamRepo extends CrudRepository<FavoriteTeam, Integer> {

    @EntityGraph(attributePaths = "flag")
    Optional<FavoriteTeam> findByChatIdAndName(String chatId, String name);

    @EntityGraph(attributePaths = "flag")
    List<FavoriteTeam> findByChatId(String chatId);
}
