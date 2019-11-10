package com.telegram.bot.csgo.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class HelpMessage extends SendMessage {
	
	public HelpMessage() {
		this.setParseMode("markdown");
		this.setText(Constants.EMOJI_INFO + " Могу посмотреть, что там нового на HLTV.org\n" 
				+ "Спрашивай, не стесняйся:\n"
				+ Constants.EMOJI_DONE + " *.хелп* - Эта информация\n" 
				+ Constants.EMOJI_DONE + " *.меню* - Меню\n"
				+ Constants.EMOJI_FIRE + " *.матчи* - Текущие/ближайшие матчи\n"
				+ Constants.EMOJI_CUP + " *.результаты* - Результаты матчей \n"
				+ Constants.EMOJI_MIL_MEDAL + " *.топ10* - Top 10 Команд\n"
				+ Constants.EMOJI_MIL_MEDAL + " *.топ20* - Top 20 Команд\n" 
				+ Constants.EMOJI_MIL_MEDAL + " *.топ30* - Top 30 Команд\n"
				+ Constants.EMOJI_SPORT_MEDAL + " *.топ10игроков* - Top 10 Игроков\n" 
				+ Constants.EMOJI_SPORT_MEDAL + " *.топ20игроков* - Top 20 Игроков\n"
				+ Constants.EMOJI_SPORT_MEDAL + " *.топ30игроков* - Top 30 Игроков");
	}

}
