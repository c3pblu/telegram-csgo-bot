package com.telegram.bot.csgo.processor;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.domain.FavoriteTeam;
import com.telegram.bot.csgo.domain.Flag;
import com.telegram.bot.csgo.model.message.HtmlMessage;
import com.telegram.bot.csgo.service.FavoriteTeamService;
import com.telegram.bot.csgo.service.FlagService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.regex.Pattern;

import static com.telegram.bot.csgo.helper.CommandHelper.TEAMS_COMMAND;
import static com.telegram.bot.csgo.helper.MessageHelper.BOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.DOUBLE_LINE_BRAKE;
import static com.telegram.bot.csgo.helper.MessageHelper.LEFT_SQUARE_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.LINE_BRAKE;
import static com.telegram.bot.csgo.helper.MessageHelper.RIGHT_SQUARE_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.TEAMS_EMPTY_FAVORITE_MESSAGE;
import static com.telegram.bot.csgo.helper.MessageHelper.TEAMS_HELP_MESSAGE;
import static com.telegram.bot.csgo.helper.MessageHelper.TEAMS_YOUR_FAVORITE_MESSAGE;
import static com.telegram.bot.csgo.helper.MessageHelper.UNBOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.WHITESPACE;
import static com.telegram.bot.csgo.model.message.Emoji.INFO;
import static com.telegram.bot.csgo.model.message.Emoji.SAD;
import static com.vdurmont.emoji.EmojiParser.parseToUnicode;

@Component
@RequiredArgsConstructor
public class FavoriteTeamsUpdateProcessor extends UpdateProcessor {

    private static final String REMOVED_STR = " removed";
    private static final String ADDED_UPDATED_STR = " added/updated";
    private static final String NOT_FOUND_STR = " is not found";
    private static final Pattern TEAMS_PLUS_PATTERN = Pattern.compile("(/teams\\+)((\\w*\\s*)*)(\\[)([A-Z]{2})(])");
    private static final Pattern TEAMS_MINUS_PATTERN = Pattern.compile("(/teams-)((\\w*\\s*)*)");

    private final BotController botController;
    private final FlagService flagService;
    private final FavoriteTeamService favoriteTeamService;

    @Override
    @Transactional
    public void process(@NonNull Update update) {
        var chatId = getChatId(update);
        if (isTeamCommand(update)) {
            botController.send(prepareMessage(chatId));
            deleteMenu(botController, update);
        }
        if (isTeamsPlusCommand(update)) {
            processPlusCommand(chatId, update.getMessage().getText());
        }
        if (isTeamsMinusCommand(update)) {
            processMinusCommand(chatId, update.getMessage().getText());
        }
    }

    private void processMinusCommand(String chatId, String text) {
        var matcher = TEAMS_MINUS_PATTERN.matcher(text);
        if (matcher.find()) {
            var teamName = matcher.group(2);
            var team = favoriteTeamService.getOneByChatIdAndName(chatId, teamName);
            team.ifPresentOrElse(
                    t -> deleteAndSendMessage(chatId, t),
                    () -> sendEntryNotFount(chatId, teamName));
        }
    }

    private void processPlusCommand(String chatId, String text) {
        var matcher = TEAMS_PLUS_PATTERN.matcher(text);
        if (matcher.find()) {
            var countryCode = matcher.group(5);
            var teamName = matcher.group(2);
            var flag = flagService.getOneByCountryCode(countryCode);
            flag.ifPresentOrElse(
                    f -> saveAndSendMessage(chatId, teamName, f),
                    () -> sendEntryNotFount(chatId, countryCode));
        }
    }

    private void deleteAndSendMessage(String chatId, FavoriteTeam team) {
        favoriteTeamService.delete(team);
        botController.send(HtmlMessage.htmlBuilder()
                .chatId(chatId)
                .text(BOLD + team.getName() + UNBOLD + REMOVED_STR)
                .build());
    }

    private void saveAndSendMessage(String chatId, String teamName, Flag flag) {
        var team = favoriteTeamService.getOneByChatIdAndName(chatId, teamName)
                .orElseGet(() -> FavoriteTeam.builder()
                        .chatId(chatId)
                        .name(teamName)
                        .build());
        team.setFlag(flag);
        favoriteTeamService.save(team);
        botController.send(HtmlMessage.htmlBuilder()
                .chatId(chatId)
                .text(BOLD + teamName + UNBOLD + ADDED_UPDATED_STR)
                .build());
    }

    private void sendEntryNotFount(String chatId, String entryName) {
        botController.send(HtmlMessage.htmlBuilder()
                .chatId(chatId)
                .text(BOLD + entryName + UNBOLD + NOT_FOUND_STR)
                .build());
    }

    private SendMessage prepareMessage(String chatId) {
        var teams = favoriteTeamService.getAllByChatId(chatId);
        if (teams.isEmpty()) {
            return HtmlMessage.htmlBuilder()
                    .chatId(chatId)
                    .text(parseToUnicode(INFO) +
                            TEAMS_YOUR_FAVORITE_MESSAGE + TEAMS_EMPTY_FAVORITE_MESSAGE +
                            parseToUnicode(SAD) + DOUBLE_LINE_BRAKE +
                            TEAMS_HELP_MESSAGE)
                    .build();
        }
        var textMessage = new StringBuilder();
        textMessage.append(parseToUnicode(INFO))
                .append(TEAMS_YOUR_FAVORITE_MESSAGE);
        teams.forEach(team -> textMessage
                .append(BOLD)
                .append(team.getName())
                .append(UNBOLD)
                .append(WHITESPACE)
                .append(LEFT_SQUARE_BRACKET)
                .append(team.getFlag().getCode())
                .append(RIGHT_SQUARE_BRACKET)
                .append(WHITESPACE)
                .append(flagService.flagUnicodeFromCountry(team.getFlag().getCode()))
                .append(LINE_BRAKE));
        textMessage.append(LINE_BRAKE)
                .append(TEAMS_HELP_MESSAGE);
        return HtmlMessage.htmlBuilder().chatId(chatId)
                .text(textMessage)
                .build();
    }

    private static boolean isTeamCommand(Update update) {
        return (update.hasMessage() && TEAMS_COMMAND.equalsIgnoreCase(update.getMessage().getText()))
                || (update.hasCallbackQuery() && TEAMS_COMMAND.equals(update.getCallbackQuery().getData()));
    }

    private static boolean isTeamsPlusCommand(Update update) {
        return update.hasMessage() && TEAMS_PLUS_PATTERN.matcher(update.getMessage().getText()).matches();
    }

    private static boolean isTeamsMinusCommand(Update update) {
        return update.hasMessage() && TEAMS_MINUS_PATTERN.matcher(update.getMessage().getText()).matches();
    }
}
