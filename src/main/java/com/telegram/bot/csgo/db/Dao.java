package com.telegram.bot.csgo.db;

import com.telegram.bot.csgo.model.DbResult;

public interface Dao {

	DbResult updateOrSaveTeam(Long chatId, String name, String countryCode);

	DbResult deleteTeam(Long chatId, String name);

	void fillAllFlags();

	void fillAllTeams();

}
