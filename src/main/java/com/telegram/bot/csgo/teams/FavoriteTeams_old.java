package com.telegram.bot.csgo.teams;

import java.util.HashMap;

import com.vdurmont.emoji.EmojiParser;

public class FavoriteTeams_old {
	
	private static HashMap<String, String> teams = new HashMap<>();

	static {
		teams.put("Nemiga", ":by:");
		teams.put("Natus Vincere", ":ru:");
		teams.put("AVANGAR", ":kz:");
		teams.put("Astralis", ":dk:");
	}
	
	public static boolean isFavorite(String name) {
		return teams.containsKey(name);
	}
	
	public static String getFlag(String name) {
		String flag = EmojiParser.parseToUnicode(teams.get(name));
		if (flag.startsWith(":")) {
			flag = EmojiParser.parseToUnicode("");
		}
		return flag;
	}
	
}
