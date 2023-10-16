package com.telegram.bot.csgo.controller;

import com.telegram.bot.csgo.config.properties.BotProperties;
import com.telegram.bot.csgo.processor.UpdateProcessor;
import com.telegram.bot.csgo.service.UpdateProcessingService;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

@Service
public class BotController extends TelegramLongPollingBot {

    private final List<UpdateProcessor> updateProcessors;
    private final BotProperties botProperties;
    private final ExecutorService threadPool = newCachedThreadPool();

    public BotController(@Lazy List<UpdateProcessor> updateProcessors, BotProperties botProperties) {
        super(botProperties.getToken());
        this.updateProcessors = updateProcessors;
        this.botProperties = botProperties;
    }

    @Override
    public void onUpdateReceived(Update update) {
        threadPool.execute(new UpdateProcessingService(this, botProperties, updateProcessors, update));
    }

    @SneakyThrows
    public void send(BotApiMethod<?> message) {
        execute(message);
    }

    @SneakyThrows
    public void sendSticker(SendSticker sticker) {
        execute(sticker);
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

}
