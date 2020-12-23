package com.telegram.bot.csgo.dao;

import java.util.List;

import com.telegram.bot.csgo.model.dao.FavoriteTeam;
import com.telegram.bot.csgo.model.dao.Flag;
import com.telegram.bot.csgo.model.dao.Result;
import com.telegram.bot.csgo.model.dao.Sticker;

public interface Dao {

	Result updateOrSaveTeam(String chatId, String name, String countryCode);

	Result deleteTeam(String chatId, String name);

	List<Flag> getFlags();

	List<FavoriteTeam> getTeams(String chatId);

	List<Sticker> getStickers();

}
