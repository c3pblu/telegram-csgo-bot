package com.telegram.bot.csgo.controller;

import com.telegram.bot.csgo.service.UpdateProcessingService;
import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ObjectProvider<UpdateProcessingService> updateProcessingFactory;

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
            if (message instanceof BotApiMethod) {
                execute((BotApiMethod<?>) message);
            }
            if (message instanceof SendSticker) {
                execute((SendSticker) message);
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
