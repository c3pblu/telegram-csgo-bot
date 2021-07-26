package com.telegram.bot.csgo.processor.impl;

import com.telegram.bot.csgo.adaptor.FlagsAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.model.HtmlMessage;
import com.telegram.bot.csgo.model.entity.FavoriteTeam;
import com.telegram.bot.csgo.model.entity.Flag;
import com.telegram.bot.csgo.processor.UpdateProcessor;
import com.telegram.bot.csgo.repository.EmojiRepository;
import com.telegram.bot.csgo.repository.FavoriteTeamsRepository;
import com.telegram.bot.csgo.repository.FlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

@Component
public class FavoriteTeamsUpdateProcessor implements UpdateProcessor {

    private final BotController botController;
    private final FlagsAdaptor flagsAdaptor;
    private final FavoriteTeamsRepository favoriteTeamsRepository;
    private final FlagRepository flagRepository;
    private final EmojiRepository emojiRepository;

    @Autowired
    public FavoriteTeamsUpdateProcessor(BotController botController, FlagsAdaptor flagsAdaptor, FavoriteTeamsRepository favoriteTeamsRepository, FlagRepository flagRepository, EmojiRepository emojiRepository) {
        this.botController = botController;
        this.flagsAdaptor = flagsAdaptor;
        this.favoriteTeamsRepository = favoriteTeamsRepository;
        this.flagRepository = flagRepository;
        this.emojiRepository = emojiRepository;
    }

    private static final String TEAMS_COMMAND = ".команды";
    private static final String TEAMS_CALLBACK = "teams";
    private static final String TEAMS_HELP_MESSAGE = "Добавить команду/изменить код страны:\n <b>.команды+Natus Vincere[RU]</b> \nУдалить команду: \n<b>.команды-Natus Vincere</b>";
    private static final String TEAM_REGEXP = "\\.команды";
    private static final String NAME_REGEXP = "([\\w]*\\s*\\.*)*";
    private static final String COUNTRY_REGEXP = "\\[[A-Z][A-Z]]";
    private static final String PLUS_REGEXP = "\\+";
    private static final String MINUS_REGEXP = "-";

    @Override
    public void process(Update update) {
        String chatId = getChatId(update);
        if (teamsCommand(update)) {
            botController.send(favoriteTeams(chatId));
            deleteMenu(botController, update);
        } else if (teamsPlusCommand(update)) {
            String text = update.getMessage().getText();
            // Insert/Update (.команды+)
            if (text.matches(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP + COUNTRY_REGEXP)) {
                String team = text.replaceAll("\\[..]", "").replaceAll(TEAM_REGEXP + PLUS_REGEXP, "").trim();
                String countryCode = text.replaceAll(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP, "").replaceAll("\\[", "")
                        .replaceAll("]", "");
                Optional<Flag> flag = flagRepository.findByCode(countryCode);
                if (flag.isPresent()) {
                    favoriteTeamsRepository.save(new FavoriteTeam(chatId, team, flag.get()));
                    botController.send(HtmlMessage.httpBuilder().setChatId(chatId).setText("<b>" + team + "</b> добавлена/обновлена").build());
                } else {
                    botController.send(HtmlMessage.httpBuilder().setChatId(chatId).setText("Код страны <b>" + countryCode + "</b> не найден").build());
                }
            }
            // Delete (.команды-)
            else if (text.matches(TEAM_REGEXP + MINUS_REGEXP + NAME_REGEXP)) {
                String team = text.replaceAll(TEAM_REGEXP + MINUS_REGEXP, "").trim();
                Optional<FavoriteTeam> dbTeam = favoriteTeamsRepository.findByChatIdAndName(chatId, team);
                if (dbTeam.isPresent()) {
                    favoriteTeamsRepository.delete(dbTeam.get());
                    botController.send(HtmlMessage.httpBuilder().setChatId(chatId).setText("<b>" + team + "</b> удалена").build());
                } else {
                    botController.send(HtmlMessage.httpBuilder().setChatId(chatId).setText("Команда <b>" + team + "</b> не найдена").build());
                }
            } else {
                botController.send(HtmlMessage.httpBuilder().setChatId(chatId).setText("Неверный формат!\nСмотрите примеры ниже!\n\n" + TEAMS_HELP_MESSAGE).build());
            }
        }
    }

    private boolean teamsCommand(Update update) {
        return (update.hasMessage() && TEAMS_COMMAND.equalsIgnoreCase(update.getMessage().getText()))
                || (update.hasCallbackQuery() && TEAMS_CALLBACK.equals(update.getCallbackQuery().getData()));
    }

    private boolean teamsPlusCommand(Update update) {
        return update.hasMessage() && update.getMessage().getText().matches(TEAM_REGEXP + ".*");
    }

    private SendMessage favoriteTeams(String chatId) {
        StringBuilder textMessage = new StringBuilder();
        List<FavoriteTeam> teams = favoriteTeamsRepository.findByChatId(chatId);
        if (teams.isEmpty()) {
            return HtmlMessage.httpBuilder()
                    .setChatId(chatId)
                    .setText(emojiRepository.getEmoji("info") + " Ваши любимые команды:\n\n<b>У вас пока нет любимых команд!</b> " + emojiRepository.getEmoji("sad") + "\n\n" + TEAMS_HELP_MESSAGE)
                    .build();
        }
        textMessage.append(emojiRepository.getEmoji("info")).append(" Ваши любимые команды:\n\n");
        teams.forEach(team -> textMessage.append("<b>").append(team.getName()).append("</b> [")
                .append(team.getCountryCode().getCode()).append("] ")
                .append(flagsAdaptor.flagUnicodeFromCountry(team.getCountryCode().getCode())).append("\n"));
        textMessage.append("\n").append(TEAMS_HELP_MESSAGE);
        return HtmlMessage.httpBuilder().setChatId(chatId).setText(textMessage).build();
    }
}
