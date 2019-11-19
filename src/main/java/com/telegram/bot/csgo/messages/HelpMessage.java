package com.telegram.bot.csgo.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class HelpMessage extends SendMessage {
	
	public HelpMessage() {
		this.setParseMode("markdown");
		this.setText(Emoji.INFO.getuCode() + " Могу посмотреть, что там нового на HLTV.org\n" 
				+ "Спрашивай, не стесняйся:\n"
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.HELP.getName() + "* - Эта информация\n" 
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.MENU.getName() + "* - Меню\n"
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.MATCHES.getName() + "* - Текущие/ближайшие матчи\n"
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.RESULTS.getName() + "* - Результаты матчей \n"
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.TOP_10.getName() + "* - Top 10 Команд\n"
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.TOP_20.getName() + "* - Top 20 Команд\n" 
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.TOP_30.getName() + "* - Top 30 Команд\n"
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.TOP_10_PLAYERS.getName() + "* - Top 10 Игроков\n" 
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.TOP_20_PLAYERS.getName() + "* - Top 20 Игроков\n"
				+ Emoji.HEAVY_CHECK_MARK.getuCode() + " *" + Commands.TOP_30_PLAYERS.getName() + "* - Top 30 Игроков");
	}

}
