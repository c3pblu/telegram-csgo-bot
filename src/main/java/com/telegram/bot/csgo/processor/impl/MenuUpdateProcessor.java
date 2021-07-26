package com.telegram.bot.csgo.processor.impl;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.processor.UpdateProcessor;
import com.telegram.bot.csgo.repository.EmojiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MenuUpdateProcessor implements UpdateProcessor {

    private static final String TOP_10 = "top10";
    private static final String TOP_20 = "top20";
    private static final String TOP_30 = "top30";
    private static final String TOP_10_PLAYERS = "top10players";
    private static final String TOP_20_PLAYERS = "top20players";
    private static final String TOP_30_PLAYERS = "top30players";
    private static final String MATCHES = "matches";
    private static final String RESULTS = "results";
    private static final String STREAMS = "streams";
    private static final String TEAMS = "teams";
    private static final String SCOREBOT = "scorebot";

    private final BotController botController;
    private final EmojiRepository emojiRepository;

    @Autowired
    public MenuUpdateProcessor(BotController botController, EmojiRepository emojiRepository) {
        this.botController = botController;
        this.emojiRepository = emojiRepository;
    }

    private static final String MENU_COMMAND = ".меню";

    @Override
    public void process(Update update) {
        if (update.hasMessage() && MENU_COMMAND.equalsIgnoreCase(update.getMessage().getText())) {
            botController.send(menuMessage(getChatId(update)));
        }
    }

    private SendMessage menuMessage(String chatId) {
        return SendMessage.builder().replyMarkup(createMenu()).text("Easy Peasy Lemon Squeezy!").chatId(chatId).build();
    }

    private InlineKeyboardMarkup createMenu() {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        String milMedal= emojiRepository.getEmoji("mil_medal");
        String sportMedal= emojiRepository.getEmoji("sport_medal");
        row1.add(InlineKeyboardButton.builder().text(emojiRepository.getEmoji("fire") + " Матчи").callbackData(MATCHES).build());
        row1.add(InlineKeyboardButton.builder().text(emojiRepository.getEmoji("tv") + " Стримы").callbackData(STREAMS).build());
        row1.add(InlineKeyboardButton.builder().text(emojiRepository.getEmoji("cup") + " Результаты").callbackData(RESULTS).build());
        row2.add(InlineKeyboardButton.builder().text(emojiRepository.getEmoji("sunglasses") + "Любимые команды").callbackData(TEAMS).build());
        row2.add(InlineKeyboardButton.builder().text(emojiRepository.getEmoji("mic") + "Трансляции").callbackData(SCOREBOT).build());
        row3.add(InlineKeyboardButton.builder().text(milMedal + "Топ 10").callbackData(TOP_10).build());
        row3.add(InlineKeyboardButton.builder().text(milMedal + "Топ 20").callbackData(TOP_20).build());
        row3.add(InlineKeyboardButton.builder().text(milMedal + "Топ 30").callbackData(TOP_30).build());
        row4.add(InlineKeyboardButton.builder().text(sportMedal + "Топ 10 Игроков").callbackData(TOP_10_PLAYERS).build());
        row4.add(InlineKeyboardButton.builder().text(sportMedal + "Топ 20 Игроков").callbackData(TOP_20_PLAYERS).build());
        row4.add(InlineKeyboardButton.builder().text(sportMedal + "Топ 30 Игроков").callbackData(TOP_30_PLAYERS).build());
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>(Arrays.asList(row1, row2, row3, row4));
        return InlineKeyboardMarkup.builder().keyboard(rowsInLine).build();
    }
}
