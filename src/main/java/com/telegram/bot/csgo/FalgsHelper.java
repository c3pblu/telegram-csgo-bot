package com.telegram.bot.csgo;

import com.vdurmont.emoji.EmojiParser;

public class FalgsHelper {

	public static String getFlgUnicode(String countryName) {
		return EmojiParser.parseToUnicode(":by:");
	}

}
