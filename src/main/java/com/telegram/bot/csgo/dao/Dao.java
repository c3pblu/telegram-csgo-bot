package com.telegram.bot.csgo.dao;

import java.util.List;

import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.model.Flag;
import com.telegram.bot.csgo.model.Sticker;

public interface Dao {

	String updateOrSaveTeam(String chatId, String name, String countryCode);

	String deleteTeam(String chatId, String name);

	List<Flag> getFlags();

	List<FavoriteTeam> getTeams(String chatId);

	List<Sticker> getStickers();

}
