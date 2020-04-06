package com.telegram.bot.csgo.dao;

import java.util.List;

import com.telegram.bot.csgo.model.DbResult;
import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.model.Flag;

public interface Dao {

	DbResult updateOrSaveTeam(Long chatId, String name, String countryCode);

	DbResult deleteTeam(Long chatId, String name);

	void fillAllFlags();

	List<Flag> getFlags();

	void fillAllTeams();

	List<FavoriteTeam> getTeams();

}
