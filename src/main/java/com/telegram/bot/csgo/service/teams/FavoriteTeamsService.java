package com.telegram.bot.csgo.service.teams;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.service.message.MessageService;

@Service
public class FavoriteTeamsService {

	private static final String TEAMS_COMMANDS = "Добавить команду/изменить код страны:\n <b>.команды+Natus Vincere[RU]</b> \nУдалить команду: \n<b>.команды-Natus Vincere</b>";

	private MessageService messageService;
	private Dao dao;

	@Autowired
	public FavoriteTeamsService(MessageService messageService, Dao dao) {
		this.messageService = messageService;
		this.dao = dao;
	}

	public SendMessage favoriteTeams(Long chatId) {
		StringBuilder textMessage = new StringBuilder();
		List<FavoriteTeam> teams = dao.getTeams(chatId);
		if (teams.isEmpty())
			return messageService.htmlMessage(Emoji.INFO + " Ваши любимые команды:\n\n<b>У вас пока нет любимых команд!</b> "
					+ Emoji.SAD + "\n\n" + TEAMS_COMMANDS);
		textMessage.append(Emoji.INFO).append(" Ваши любимые команды:\n\n");
		teams.stream()
				.forEach(team -> textMessage.append("<b>").append(team.getName()).append("</b> [")
						.append(team.getCountryCode().getCode()).append("] ")
						.append(messageService.flagUnicodeFromCountry(team.getCountryCode().getCode())).append("\n"));
		textMessage.append("\n").append(TEAMS_COMMANDS);
		return messageService.htmlMessage(textMessage);
	}

	public String updateOrSaveTeam(Long chatId, String name, String countryCode) {
		return dao.updateOrSaveTeam(chatId, name, countryCode);
	}

	public String deleteTeam(Long chatId, String name) {
		return dao.deleteTeam(chatId, name);
	}

}
