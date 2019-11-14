package com.telegram.bot.csgo.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.model.Constants;

public class HelpMessage extends SendMessage {
	
	public HelpMessage() {
		this.setParseMode("markdown");
		this.setText(Constants.EMOJI_INFO + " Могу посмотреть, что там нового на HLTV.org\n" 
				+ "Спрашивай, не стесняйся:\n"
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.HELP + "* - Эта информация\n" 
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.MENU + "* - Меню\n"
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.MATCHES + "* - Текущие/ближайшие матчи\n"
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.RESULTS + "* - Результаты матчей \n"
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.TOP_10 + "* - Top 10 Команд\n"
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.TOP_20 + "* - Top 20 Команд\n" 
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.TOP_30 + "* - Top 30 Команд\n"
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.TOP_10_PLAYERS + "* - Top 10 Игроков\n" 
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.TOP_20_PLAYERS + "* - Top 20 Игроков\n"
				+ Constants.EMOJI_HEAVY_CHECK_MARK + " *" + Constants.TOP_30_PLAYERS + "* - Top 30 Игроков");
	}

}
