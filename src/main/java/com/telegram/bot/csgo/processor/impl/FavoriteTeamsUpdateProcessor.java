package com.telegram.bot.csgo.processor.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.adaptor.FlagsAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.model.HtmlMessage;
import com.telegram.bot.csgo.model.dao.FavoriteTeam;
import com.telegram.bot.csgo.model.dao.Result;
import com.telegram.bot.csgo.processor.UpdateProcessor;

@Component
public class FavoriteTeamsUpdateProcessor implements UpdateProcessor {

    private BotController botController;
    private FlagsAdaptor flagsAdaptor;
    private Dao dao;

    @Autowired
    public FavoriteTeamsUpdateProcessor(BotController botController, FlagsAdaptor flagsAdaptor, Dao dao) {
        this.botController = botController;
        this.flagsAdaptor = flagsAdaptor;
        this.dao = dao;
    }

    private static final String TEAMS_HELP_COMMAND = ".команды";
    private static final String TEAMS_HELP_CALLBACK = "teams";
    private static final String TEAMS_HELP_MESSAGE = "Добавить команду/изменить код страны:\n <b>.команды+Natus Vincere[RU]</b> \nУдалить команду: \n<b>.команды-Natus Vincere</b>";
    private static final String TEAM_REGEXP = "\\.команды";
    private static final String NAME_REGEXP = "([\\w]*\\s{0,}\\.{0,})*";
    private static final String COUNTRY_REGEXP = "\\[[A-Z][A-Z]\\]";
    private static final String PLUS_REGEXP = "\\+";
    private static final String MINUS_REGEXP = "\\-";

    @Override
    public void process(Update update) {
        String chatId = getChatId(update);
        // Help Message
        if ((update.hasMessage() && TEAMS_HELP_COMMAND.equalsIgnoreCase(update.getMessage().getText()))
                || (update.hasCallbackQuery() && TEAMS_HELP_CALLBACK.equals(update.getCallbackQuery().getData()))) {
            botController.send(favoriteTeams(chatId));
            deleteMenu(botController, update);
        } else if (update.hasMessage() && update.getMessage().getText().matches(TEAM_REGEXP + ".*")) {
            String text = update.getMessage().getText();
            // Insert/Update (.команды+)
            if (text.matches(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP + COUNTRY_REGEXP)) {
                String team = text.replaceAll("\\[..\\]", "").replaceAll(TEAM_REGEXP + PLUS_REGEXP, "").trim();
                String countryCode = text.replaceAll(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP, "").replaceAll("\\[", "")
                        .replaceAll("\\]", "");
                Result dbResult = dao.updateOrSaveTeam(chatId, team, countryCode);
                botController.send(daoResultMessage(chatId, dbResult, team));
            }
            // Delete (.команды-)
            else if (text.matches(TEAM_REGEXP + MINUS_REGEXP + NAME_REGEXP)) {
                String team = text.replaceAll(TEAM_REGEXP + MINUS_REGEXP, "").trim();
                Result dbResult = dao.deleteTeam(chatId, team);
                botController.send(daoResultMessage(chatId, dbResult, team));
            } else {
                botController.send(teamsHelpMessage(chatId));
            }
        }
    }

    private SendMessage teamsHelpMessage(String chatId) {
        return new HtmlMessage(chatId, "Неверный формат!\nСмотрите примеры ниже!\n\n" + TEAMS_HELP_MESSAGE);
    }

    private SendMessage favoriteTeams(String chatId) {
        StringBuilder textMessage = new StringBuilder();
        List<FavoriteTeam> teams = dao.getTeams(chatId);
        if (teams.isEmpty()) {
            return new HtmlMessage(chatId,
                    Emoji.INFO + " Ваши любимые команды:\n\n<b>У вас пока нет любимых команд!</b> " + Emoji.SAD + "\n\n"
                            + TEAMS_HELP_MESSAGE);
        }
        textMessage.append(Emoji.INFO).append(" Ваши любимые команды:\n\n");
        teams.stream()
                .forEach(team -> textMessage.append("<b>").append(team.getName()).append("</b> [")
                        .append(team.getCountryCode().getCode()).append("] ")
                        .append(flagsAdaptor.flagUnicodeFromCountry(team.getCountryCode().getCode())).append("\n"));
        textMessage.append("\n").append(TEAMS_HELP_MESSAGE);
        return new HtmlMessage(chatId, textMessage);
    }

    private SendMessage daoResultMessage(String chatId, Result dbResult, String name) {
        HtmlMessage message = new HtmlMessage(chatId, dbResult.getMessage());
        if (Result.DELETED.equals(dbResult) || Result.UPDATED.equals(dbResult) || Result.ALREADY_EXIST.equals(dbResult)
                || Result.INSERTED.equals(dbResult)) {
            message.setText("<b>" + name + "</b> " + dbResult.getMessage());
        }
        return message;
    }

}
