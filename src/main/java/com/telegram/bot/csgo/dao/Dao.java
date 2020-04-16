package com.telegram.bot.csgo.dao;

import java.util.List;

import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.model.Flag;
import com.telegram.bot.csgo.model.Sticker;

public interface Dao {

	String updateOrSaveTeam(Long chatId, String name, String countryCode);

	String deleteTeam(Long chatId, String name);

	List<Flag> getFlags();

	List<FavoriteTeam> getTeams(Long chatId);

	List<Sticker> getStickers();

}
