package com.telegram.bot.csgo.model;

import com.vdurmont.emoji.EmojiParser;

public final class Constants {
	
	public static final String TOP_10 = "top10";
	public static final String TOP_20 = "top20";
	public static final String TOP_30 = "top30";
	public static final String TOP_10_PLAYERS = "top10players";
	public static final String TOP_20_PLAYERS = "top20players";
	public static final String TOP_30_PLAYERS = "top30players";
	public static final String MATCHES = "matches";
	public static final String RESULTS = "results";
	public static final String OOPS = "Упс, ты слишком долго думал парень!";
	public static final String MATCHES_FOR_TODAY = "Матчи на сегодня:";
	public static final String RESULTS_FOR_TODAY = "Результаты за сегодня:";
	public final static String USER_AGENT_NAME = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
	public final static String HLTV = "https://www.hltv.org";
	public final static String HTML = "html";
	public final static String EXCEPTION_MSG = "Can't get data from site";
	
	// Emoji
	public final static String EMOJI_INFO = "\u2139";
	public final static String EMOJI_DONE = "\u2705";
	public final static String EMOJI_EXCL_MARK = "\u2757";
	public final static String EMOJI_CUP = "\uD83C\uDFC6";
	public final static String EMOJI_VS = "\uD83C\uDD9A";
	public final static String EMOJI_SQUARE = "\u25AB";
	public final static String EMOJI_FIRE = "\uD83D\uDD25";
	public final static String EMOJI_MIL_MEDAL = EmojiParser.parseToUnicode(":military_medal:");
	public final static String EMOJI_SPORT_MEDAL = EmojiParser.parseToUnicode(":sports_medal:");
	public final static String EMOJI_STAR = EmojiParser.parseToUnicode(":star:");
	public final static String EMOJI_HEAVY_CHECK_MARK = EmojiParser.parseToUnicode(":heavy_check_mark:");
	
	private Constants() {
	}

}
