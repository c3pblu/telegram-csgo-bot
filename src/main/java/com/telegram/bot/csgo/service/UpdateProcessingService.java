package com.telegram.bot.csgo.service;

import java.util.List;
import static java.lang.String.valueOf;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.time.Instant.now;
import com.telegram.bot.csgo.config.properties.BotProperties;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.model.message.HtmlMessage;
import com.telegram.bot.csgo.processor.UpdateProcessor;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class UpdateProcessingService implements Runnable {

    private static final String TIMEOUT_MESSAGE = "Oops, you are too slow! Try again";

    private final BotController botController;
    private final BotProperties botProperties;
    private final List<UpdateProcessor> updateProcessors;
    private final Update update;

    @Override
    public void run() {
        currentThread().setName(valueOf(currentTimeMillis()));

        if (!isExpired()) {
            updateProcessors.forEach(processor -> processor.process(update));
        }
    }

    private boolean isExpired() {
        if (update.hasMessage()) {
            var responseTime = now().getEpochSecond() - update.getMessage().getDate();
            return responseTime > botProperties.getMessage().getTimeout();
        }
        if (update.hasCallbackQuery()) {
            var responseTime = now().getEpochSecond() - update.getCallbackQuery().getMessage().getDate();
            if (responseTime > botProperties.getCallbackTimeout()) {
                var chatId = valueOf(update.getCallbackQuery().getMessage().getChatId());
                botController.send(new HtmlMessage(chatId, TIMEOUT_MESSAGE));
                botController.send(new DeleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId()));
                return true;
            }
        }
        return false;
    }
}
