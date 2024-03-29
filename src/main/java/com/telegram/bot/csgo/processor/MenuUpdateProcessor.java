package com.telegram.bot.csgo.processor;

import com.telegram.bot.csgo.controller.BotController;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static com.telegram.bot.csgo.helper.CommandHelper.MATCHES_COMMAND;
import static com.telegram.bot.csgo.helper.CommandHelper.MENU_COMMAND;
import static com.telegram.bot.csgo.helper.CommandHelper.RESULTS_COMMAND;
import static com.telegram.bot.csgo.helper.CommandHelper.STREAMS_COMMAND;
import static com.telegram.bot.csgo.helper.CommandHelper.TEAMS_COMMAND;
import static com.telegram.bot.csgo.helper.CommandHelper.TOP_10_CALLBACK;
import static com.telegram.bot.csgo.helper.CommandHelper.TOP_10_PLAYERS_CALLBACK;
import static com.telegram.bot.csgo.helper.CommandHelper.TOP_20_CALLBACK;
import static com.telegram.bot.csgo.helper.CommandHelper.TOP_20_PLAYERS_CALLBACK;
import static com.telegram.bot.csgo.helper.CommandHelper.TOP_30_CALLBACK;
import static com.telegram.bot.csgo.helper.CommandHelper.TOP_30_PLAYERS_CALLBACK;
import static com.telegram.bot.csgo.model.message.Emoji.CUP;
import static com.telegram.bot.csgo.model.message.Emoji.FIRE;
import static com.telegram.bot.csgo.model.message.Emoji.MIL_MEDAL;
import static com.telegram.bot.csgo.model.message.Emoji.SPORT_MEDAL;
import static com.telegram.bot.csgo.model.message.Emoji.SUNGLASSES;
import static com.telegram.bot.csgo.model.message.Emoji.TV;
import static com.vdurmont.emoji.EmojiParser.parseToUnicode;
import static org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton.builder;

@Component
@RequiredArgsConstructor
public class MenuUpdateProcessor extends UpdateProcessor {

    private static final String MENU_TEXT = "Easy Peasy Lemon Squeezy!";
    private static final String MATCHES_STR = " Matches";
    private static final String STREAMS_STR = " Streams";
    private static final String RESULTS_STR = " Results";
    private static final String FAVORITE_TEAMS_STR = " Favorite Teams";
    private static final String TEN_STR = " 10";
    private static final String TWENTY_STR = " 20";
    private static final String THIRTY_STR = " 30";
    private static final String TOP_STR = "Top";
    private static final String TEAMS_STR = " Teams";
    private static final String PLAYERS_STR = " Players";
    private static final String TOP_10_TEAMS_STR = TOP_STR + TEN_STR + TEAMS_STR;
    private static final String TOP_20_TEAMS_STR = TOP_STR + TWENTY_STR + TEAMS_STR;
    private static final String TOP_30_TEAMS_STR = TOP_STR + THIRTY_STR + TEAMS_STR;
    private static final String TOP_10_PLAYERS_STR = TOP_STR + TEN_STR + PLAYERS_STR;
    private static final String TOP_20_PLAYERS_STR = TOP_STR + TWENTY_STR + PLAYERS_STR;
    private static final String TOP_30_PLAYERS_STR = TOP_STR + THIRTY_STR + PLAYERS_STR;

    private final BotController botController;

    @Override
    public void process(@NonNull Update update) {
        if (isMenuCommand(update)) {
            botController.send(menuMessage(getChatId(update)));
        }
    }

    private SendMessage menuMessage(String chatId) {
        return SendMessage.builder()
                .replyMarkup(createMenu())
                .text(MENU_TEXT)
                .chatId(chatId)
                .build();
    }

    private InlineKeyboardMarkup createMenu() {
        var row1 = List.of(
                builder().text(parseToUnicode(FIRE) + MATCHES_STR)
                        .callbackData(MATCHES_COMMAND)
                        .build(),
                builder().text(parseToUnicode(CUP) + RESULTS_STR)
                        .callbackData(RESULTS_COMMAND)
                        .build());

        var row2 = List.of(
                builder().text(parseToUnicode(SUNGLASSES) + FAVORITE_TEAMS_STR)
                        .callbackData(TEAMS_COMMAND)
                        .build(),
                builder().text(parseToUnicode(TV) + STREAMS_STR)
                        .callbackData(STREAMS_COMMAND)
                        .build());

        var milMedal = parseToUnicode(MIL_MEDAL);
        var row3 = List.of(
                builder().text(milMedal + TOP_10_TEAMS_STR)
                        .callbackData(TOP_10_CALLBACK)
                        .build(),
                builder().text(milMedal + TOP_20_TEAMS_STR)
                        .callbackData(TOP_20_CALLBACK)
                        .build(),
                builder().text(milMedal + TOP_30_TEAMS_STR)
                        .callbackData(TOP_30_CALLBACK)
                        .build());

        var sportMedal = parseToUnicode(SPORT_MEDAL);
        var row4 = List.of(
                builder().text(sportMedal + TOP_10_PLAYERS_STR)
                        .callbackData(TOP_10_PLAYERS_CALLBACK)
                        .build(),
                builder().text(sportMedal + TOP_20_PLAYERS_STR)
                        .callbackData(TOP_20_PLAYERS_CALLBACK)
                        .build(),
                builder().text(sportMedal + TOP_30_PLAYERS_STR)
                        .callbackData(TOP_30_PLAYERS_CALLBACK)
                        .build());
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3, row4))
                .build();
    }

    private static boolean isMenuCommand(Update update) {
        return update.hasMessage() && MENU_COMMAND.equalsIgnoreCase(update.getMessage().getText());
    }
}
