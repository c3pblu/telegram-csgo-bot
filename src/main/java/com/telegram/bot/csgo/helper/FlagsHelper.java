package com.telegram.bot.csgo.helper;

import com.vdurmont.emoji.EmojiParser;

public final class FlagsHelper {
	
	private FlagsHelper() {
		
	}

	public static String getFlgUnicode(String countryName) {
		return EmojiParser.parseToUnicode(":by:");
	}

}
