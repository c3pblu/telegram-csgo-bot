package com.telegram.bot.csgo.controller;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegram.bot.csgo.messages.Message;
import com.telegram.bot.csgo.messages.MessageBuilder;
import com.telegram.bot.csgo.model.CallBackData;
import com.telegram.bot.csgo.model.Commands;
import com.telegram.bot.csgo.model.Streams;

@Component
public class Bot extends TelegramLongPollingBot {

    private static final String OOPS = "Упс, ты слишком долго думал парень!";
    private static final String NEXT_PAGE = "Go to next Page?";
    private static final String TEAM_REGEXP = "\\.[к][о][м][а][н][д][ы]";
    private static final String NAME_REGEXP = "([\\w]*\\s{0,}\\.{0,})*";
    private static final String COUNTRY_REGEXP = "\\[[A-Z][A-Z]\\]";
    private static final String PLUS_REGEXP = "\\+";
    private static final String MINUS_REGEXP = "\\-";

    @Autowired
    private MessageBuilder messageBuilder;
    @Autowired
    private Message message;

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
    @Value(value = "${bot.message.uniq.count}")
    private Integer uniqCount;

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
            if (text.equalsIgnoreCase(Commands.HELP.getName())) {
                sendMessage(chatId, message.createHelpMessage());
            }
            // Menu
            else if (text.equalsIgnoreCase(Commands.MENU.getName())) {
                sendMessage(chatId, message.createMenuMessage());
            }
            // Matches
            else if (text.equalsIgnoreCase(Commands.MATCHES.getName())) {
                sendMessage(chatId, messageBuilder.matches(chatId));
            }
            // Results
            else if (text.equalsIgnoreCase(Commands.RESULTS.getName())) {
                sendMessage(chatId, messageBuilder.results(chatId));
            }
            // Top Players
            else if (text.equalsIgnoreCase(Commands.TOP_10_PLAYERS.getName())
                    || text.equalsIgnoreCase(Commands.TOP_20_PLAYERS.getName())
                    || text.equalsIgnoreCase(Commands.TOP_30_PLAYERS.getName())) {
                Integer count = Integer.parseInt(text.substring(4, 6));
                sendMessage(chatId, messageBuilder.topPlayers(count));
            }
            // Top Teams
            else if (text.equalsIgnoreCase(Commands.TOP_10.getName()) || text.equalsIgnoreCase(Commands.TOP_20.getName())
                    || text.equalsIgnoreCase(Commands.TOP_30.getName())) {
                Integer count = Integer.parseInt(update.getMessage().getText().substring(4));
                sendMessage(chatId, messageBuilder.topTeams(count));
            }
            // Twitch streams
            else if (text.equalsIgnoreCase(Commands.STREAMS.getName())) {
                Streams streams = messageBuilder.twitch("");
                sendMessage(chatId, message.createTextMessage(streams.getMessage()));
                sendMessage(chatId, message.createNextPageMessage(streams.getNextPageId()));
            }
            // Favorite Teams
            else if (text.equalsIgnoreCase(Commands.TEAMS.getName())) {
                sendMessage(chatId, messageBuilder.getAllTeams(chatId));
            }

            else if (text.matches(TEAM_REGEXP + ".*")) {
                // Insert/Update (.команды+)
                if (text.matches(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP + COUNTRY_REGEXP)) {
                    String team = text.replaceAll("\\[..\\]", "")
                            .replaceAll(TEAM_REGEXP + PLUS_REGEXP, "").trim();
                    String countryCode = text.replaceAll(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP, "")
                            .replaceAll("\\[", "").replaceAll("\\]", "");
                    sendMessage(chatId, messageBuilder.updateFavoriteTeam(chatId, team, countryCode));
                }
                // Delete (.команды-)
                else if (text.matches(TEAM_REGEXP + MINUS_REGEXP + NAME_REGEXP)) {
                    String team = text.replaceAll(TEAM_REGEXP + MINUS_REGEXP, "").trim();
                    sendMessage(chatId, messageBuilder.deleteTeam(chatId, team));
                }
                else {
                    sendMessage(chatId, messageBuilder.teamsFormat());
                }
            }
            // Private and mentioned message
            else if (StringUtils.startsWith(update.getMessage().getText(), "@" + botName)
                    || update.getMessage().getChat().isUserChat()) {
                sendMessage(chatId, message.createBotMessage(uniqCount));
            }

        }

        // Check call back from Menu
        if (update.getCallbackQuery() != null) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            CallbackQuery callBack = update.getCallbackQuery();
            if (isTimeout(callBack)) {
                sendMessage(chatId, new SendMessage().setText(OOPS));
                deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
                return;
            }
            String data = callBack.getData();
            if (data.equals(CallBackData.TOP_10.getName())) {
                sendMessage(chatId, messageBuilder.topTeams(10));
            }
            if (data.equals(CallBackData.TOP_20.getName())) {
                sendMessage(chatId, messageBuilder.topTeams(20));
            }
            if (data.equals(CallBackData.TOP_30.getName())) {
                sendMessage(chatId, messageBuilder.topTeams(30));
            }
            if (data.equals(CallBackData.TOP_10_PLAYERS.getName())) {
                sendMessage(chatId, messageBuilder.topPlayers(10));
            }
            if (data.equals(CallBackData.TOP_20_PLAYERS.getName())) {
                sendMessage(chatId, messageBuilder.topPlayers(20));
            }
            if (data.equals(CallBackData.TOP_30_PLAYERS.getName())) {
                sendMessage(chatId, messageBuilder.topPlayers(30));
            }
            if (data.equals(CallBackData.MATCHES.getName())) {
                sendMessage(chatId, messageBuilder.matches(chatId));
            }
            if (data.equals(CallBackData.RESULTS.getName())) {
                sendMessage(chatId, messageBuilder.results(chatId));
            }
            if (data.equals(CallBackData.TEAMS.getName())) {
                sendMessage(chatId, messageBuilder.getAllTeams(chatId));
            }
            if (data.equals(CallBackData.STREAMS.getName())
                    || update.getCallbackQuery().getMessage().getText().equals(NEXT_PAGE)) {
                Streams streams = messageBuilder.twitch(data.replace(CallBackData.STREAMS.getName(), ""));
                sendMessage(chatId, message.createTextMessage(streams.getMessage()));
                sendMessage(chatId, message.createNextPageMessage(streams.getNextPageId()));
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
    
    private void sendMessage(Long chatId, SendSticker msg) {
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
        sendMessage(schedulerChatId, messageBuilder.matchesForToday());
        sendMessage(schedulerChatId, messageBuilder.matches(schedulerChatId));
    }

    @Scheduled(cron = "${bot.scheduler.results.cron}")
    private void todayResultsScheduler() {
        sendMessage(schedulerChatId, messageBuilder.resultsForToday());
        sendMessage(schedulerChatId, messageBuilder.results(schedulerChatId));
    }

}
