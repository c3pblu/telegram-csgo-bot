package com.telegram.bot.csgo.messages;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class NextPage extends SendMessage {

    private InlineKeyboardMarkup MARK_UP_IN_LINE = new InlineKeyboardMarkup();
    private List<List<InlineKeyboardButton>> ROWS_IN_LINE = new ArrayList<>();
    private List<InlineKeyboardButton> ROW_1 = new ArrayList<>();

    public NextPage(String nextPageId) {
        this.ROW_1.add(new InlineKeyboardButton().setText("Next 20 Streams").setCallbackData(nextPageId));
        this.ROWS_IN_LINE.add(ROW_1);
        this.MARK_UP_IN_LINE.setKeyboard(ROWS_IN_LINE);
        this.setReplyMarkup(MARK_UP_IN_LINE);
        this.setText("Go to next Page?");
    }

}
