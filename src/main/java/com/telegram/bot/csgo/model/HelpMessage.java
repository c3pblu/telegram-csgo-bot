package com.telegram.bot.csgo.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class HelpMessage extends SendMessage {
	
	public HelpMessage() {
		this.setParseMode("markdown");
		this.setText("\u2139 Могу посмотреть, что там нового на HLTV.org\n" 
				+ "Спрашивай, не стесняйся:\n"
				+ "\u2705 *.хелп* - Эта информация\n" 
				+ "\u2705 *.меню* - Меню\n"
				+ "\u2705 *.матчи* - Текущие/ближайшие матчи\n"
				+ "\u2705 *.результаты* - Результаты матчей \n"
				+ "\u2705 *.топ10* - Top 10 Команд\n"
				+ "\u2705 *.топ20* - Top 20 Команд\n" 
				+ "\u2705 *.топ30* - Top 30 Команд\n"
				+ "\u2705 *.топ10игроков* - Top 10 Игроков\n" 
				+ "\u2705 *.топ20игроков* - Top 20 Игроков\n"
				+ "\u2705 *.топ30игроков* - Top 30 Игроков");
	}

}
