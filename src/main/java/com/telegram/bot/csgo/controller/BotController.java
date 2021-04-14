package com.telegram.bot.csgo.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegram.bot.csgo.service.UpdateProcessingService;

import lombok.Getter;

@Service
@Getter
public class BotController extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.callback.timeout}")
    private Long callBackTimeout;
    @Value("${bot.message.timeout}")
    private Long messageTimeout;
    ExecutorService threadPool = Executors.newCachedThreadPool();
    private ObjectProvider<UpdateProcessingService> updateProcessingFactory;

    @Autowired
    public BotController(ObjectProvider<UpdateProcessingService> updateProcessingFactory) {
        this.updateProcessingFactory = updateProcessingFactory;
    }

    @Override
    public void onUpdateReceived(Update update) {
        threadPool.execute(updateProcessingFactory.getObject(update));
    }

    public void send(PartialBotApiMethod<?> message) {
        try {
            if (message instanceof SendMessage) {
                SendMessage sendMessage = (SendMessage) message;
                execute(sendMessage);
            }
            if (message instanceof SendSticker) {
                SendSticker stickerMessage = (SendSticker) message;
                execute(stickerMessage);
            }
            if (message instanceof DeleteMessage) {
                DeleteMessage deleteMessage = (DeleteMessage) message;
                execute(deleteMessage);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

}
