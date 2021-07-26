package com.telegram.bot.csgo.repository;

import com.telegram.bot.csgo.model.entity.FavoriteTeam;
import com.telegram.bot.csgo.model.entity.FavoriteTeamPK;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteTeamsRepository extends CrudRepository<FavoriteTeam, FavoriteTeamPK> {

    Optional<FavoriteTeam> findByChatIdAndName(String chatId, String name);

    List<FavoriteTeam> findByChatId(String chatId);

}
