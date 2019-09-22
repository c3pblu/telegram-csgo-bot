package com.telegram.bot.csgo;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.telegram.bot.csgo.model.Constants;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return Constants.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return Constants.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            // Help
            if (update.getMessage().getText().equalsIgnoreCase(".хелп")) {
                sendMessage(chatId, MessageHelper.help());
            }
            // Top 10
            if (update.getMessage().getText().equalsIgnoreCase(".топ10")) {
                sendMessage(chatId, MessageHelper.topTeams(10));
            }
            // Top 30
            if (update.getMessage().getText().equalsIgnoreCase(".топ30")) {
                    sendMessage(chatId, MessageHelper.topTeams(30));
            }
            // Private message
            if (StringUtils.startsWith(update.getMessage().getText(), "@" + Constants.BOT_NAME)) {
                sendMessage(chatId, MessageHelper.toBot(update.getMessage().getFrom().getUserName()));
            }
        }
    }

    private void sendMessage(Long chatId, SendMessage msg) {
        msg.setChatId(chatId);
        try {
            execute(msg); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
