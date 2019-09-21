package com.telegram.bot.csgo.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class Help extends SendMessage {

	public Help() {
		this.setParseMode("markdown");
		this.setText("\u2139 Могу посмотреть, что там нового на HLTV.org\n" +
				"\n" +
				"Спрашивай, не стесняйся:\n" +
				"\u2705 *.хелп* - эта информация\n" +
				"\u2705 *.топ10* - Top 10 Команд\n" +
				"\u2705 *.топ100* - Top 100 Команд\n" +
				"\u2705 *.топ100игроков* - Top 100 Игроков");
	}
	
	
}
