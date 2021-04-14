package com.telegram.bot.csgo.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.model.HtmlMessage;
import com.telegram.bot.csgo.update.processor.UpdateProcessor;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UpdateProcessingService implements Runnable {

    private BotController botController;
    private List<UpdateProcessor> updateProcessors;
    private Update update;

    @Autowired
    public UpdateProcessingService(BotController botController, List<UpdateProcessor> updateProcessors) {
        this.botController = botController;
        this.updateProcessors = updateProcessors;
    }

    @Override
    public void run() {
        if (!isTimeout(update)) {
            updateProcessors.forEach(processor -> processor.process(update));
            System.gc();
        }
    }

    private boolean isTimeout(Update update) {
        if (update.hasMessage()) {
            long responseTime = Instant.now().getEpochSecond() - update.getMessage().getDate();
            if (responseTime > botController.getMessageTimeout()) {
                return true;
            }
        }
        if (update.hasCallbackQuery()) {
            long responseTime = Instant.now().getEpochSecond() - update.getCallbackQuery().getMessage().getDate();
            if (responseTime > botController.getCallBackTimeout()) {
                String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
                botController.send(new HtmlMessage(chatId, "Упс, ты слишком долго думал парень!"));
                botController.send(new DeleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId()));
                return true;
            }
        }
        return false;
    }

    public UpdateProcessingService setUpdate(Update update) {
        this.update = update;
        return this;
    }

}
