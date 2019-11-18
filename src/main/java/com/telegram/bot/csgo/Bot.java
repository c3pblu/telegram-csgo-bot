package com.telegram.bot.csgo;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegram.bot.csgo.messages.BotMessages;
import com.telegram.bot.csgo.messages.HelpMessage;
import com.telegram.bot.csgo.messages.MenuMessage;
import com.telegram.bot.csgo.messages.MessageBuilder;
import com.telegram.bot.csgo.messages.NextPage;
import com.telegram.bot.csgo.messages.TextMessage;
import com.telegram.bot.csgo.model.Constants;
import com.telegram.bot.csgo.twitch.Streams;

@Component
public class Bot extends TelegramLongPollingBot {

    @Autowired
    private BotMessages botMessages;
    @Autowired
    private MessageBuilder messages;

    @Value(value = "${bot.callback.timeout}")
    private Long callBackTimeout;
    @Value(value = "${bot.name}")
    private String botName;
    @Value(value = "${bot.token}")
    private String botToken;
    @Value(value = "${bot.scheduler.chat.id}")
    private Long schedulerChatId;
    @Value(value = "${bot.message.timeout}")
    private Long messageTimeout;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // If message timeout - skip message
            if (isTimeout(update)) {
                return;
            }

            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            // Help
            if (text.equalsIgnoreCase(Constants.HELP)) {
                sendMessage(chatId, new HelpMessage());
            }
            // Menu
            else if (text.equalsIgnoreCase(Constants.MENU)) {
                sendMessage(chatId, new MenuMessage());
            }
            // Matches
            else if (text.equalsIgnoreCase(Constants.MATCHES)) {
                sendMessage(chatId, messages.matches());
            }
            // Results
            else if (text.equalsIgnoreCase(Constants.RESULTS)) {
                sendMessage(chatId, messages.results());
            }
            // Top Players
            else if (text.equalsIgnoreCase(Constants.TOP_10_PLAYERS) || text.equalsIgnoreCase(Constants.TOP_20_PLAYERS)
                    || text.equalsIgnoreCase(Constants.TOP_30_PLAYERS)) {
                Integer count = Integer.parseInt(text.substring(4, 6));
                sendMessage(chatId, messages.topPlayers(count));
            }
            // Top Teams
            else if (text.equalsIgnoreCase(Constants.TOP_10) || text.equalsIgnoreCase(Constants.TOP_20)
                    || text.equalsIgnoreCase(Constants.TOP_30)) {
                Integer count = Integer.parseInt(update.getMessage().getText().substring(4));
                sendMessage(chatId, messages.topTeams(count));
            }
            // Private and mentioned message
            else if (StringUtils.startsWith(update.getMessage().getText(), "@" + botName)
                    || update.getMessage().getChat().isUserChat()) {
                sendMessage(chatId, messages.cite());
            }
            // Twitch streams
            else if (text.equalsIgnoreCase(Constants.STREAMS)) {
                Streams streams = messages.twitch("");
                sendMessage(chatId, new TextMessage(streams.getMessage()));
                sendMessage(chatId, new NextPage(streams.getNextPageId()));
            }
        }

        // Check call back from Menu
        if (update.getCallbackQuery() != null) {

            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            CallbackQuery callBack = update.getCallbackQuery();

            if (isTimeout(callBack)) {
                sendMessage(chatId, new SendMessage().setText(Constants.OOPS));
                deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
                return;
            }

            String data = callBack.getData();

            if (data.equals(Constants.DATA_TOP_10)) {
                sendMessage(chatId, messages.topTeams(10));
            }
            if (data.equals(Constants.DATA_TOP_20)) {
                sendMessage(chatId, messages.topTeams(20));
            }
            if (data.equals(Constants.DATA_TOP_30)) {
                sendMessage(chatId, messages.topTeams(30));
            }
            if (data.equals(Constants.DATA_TOP_10_PLAYERS)) {
                sendMessage(chatId, messages.topPlayers(10));
            }
            if (data.equals(Constants.DATA_TOP_20_PLAYERS)) {
                sendMessage(chatId, messages.topPlayers(20));
            }
            if (data.equals(Constants.DATA_TOP_30_PLAYERS)) {
                sendMessage(chatId, messages.topPlayers(30));
            }
            if (data.equals(Constants.DATA_MATCHES)) {
                sendMessage(chatId, messages.matches());
            }
            if (data.equals(Constants.DATA_RESULTS)) {
                sendMessage(chatId, messages.results());
            }
            if (data.equals(Constants.DATA_STREAMS)
                    || update.getCallbackQuery().getMessage().getText().equals("Go to next Page?")) {

                Streams streams = messages.twitch(data.replace("streams", ""));
                sendMessage(chatId, new TextMessage(streams.getMessage()));
                sendMessage(chatId, new NextPage(streams.getNextPageId()));
            }

            deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
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

    private void deleteMessage(Long chatId, Integer msgId) {
        try {
            execute(new DeleteMessage(chatId, msgId));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isTimeout(Object obj) {
        Update update = null;
        CallbackQuery callBack = null;

        if (obj instanceof Update) {
            update = (Update) obj;
        }

        if (obj instanceof CallbackQuery) {
            callBack = (CallbackQuery) obj;
        }

        if (update != null) {
            long responseTime = Instant.now().getEpochSecond() - update.getMessage().getDate();
            if (responseTime > messageTimeout) {
                return true;
            }
        }

        if (callBack != null) {
            long responseTime = Instant.now().getEpochSecond() - callBack.getMessage().getDate();
            if (responseTime > callBackTimeout) {
                return true;
            }
        }
        return false;
    }

    @Scheduled(cron = "${bot.scheduler.matches.cron}")
    private void todayMatchesScheduler() {
        sendMessage(schedulerChatId, messages.matchesForToday());
        sendMessage(schedulerChatId, messages.matches());
    }

    @Scheduled(cron = "${bot.scheduler.results.cron}")
    private void todayResultsScheduler() {
        sendMessage(schedulerChatId, messages.resultsForToday());
        sendMessage(schedulerChatId, messages.results());
    }

}
