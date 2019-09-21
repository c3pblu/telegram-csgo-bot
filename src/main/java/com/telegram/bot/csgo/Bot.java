package com.telegram.bot.csgo;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegram.bot.csgo.model.Constants;
import com.telegram.bot.csgo.model.Help;
import com.telegram.bot.csgo.model.Top10;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import static com.telegram.bot.csgo.Http.top30;

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
                sendMessage(chatId, new Help());
            }

            // Top 10
            if (update.getMessage().getText().equalsIgnoreCase(".топ30")) {
                try {
                    sendMessage(chatId, Http.top30());
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            // Private message
            if (StringUtils.startsWith(update.getMessage().getText(), "@" + Constants.BOT_NAME)) {
                String who = update.getMessage().getFrom().getUserName();
                sendMessage(chatId, "Ну все... Молись @" + who + " сейчас отхватишь! \uD83D\uDCAA");


            }



        }

    }

    private void sendMessage(Long chatId, String msg) {
        SendMessage newMessage = new SendMessage();
        newMessage.setChatId(chatId);
        newMessage.setText(msg);
        try {
            execute(newMessage); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
