package com.telegram.bot.csgo.controller;

import java.util.List;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newCachedThreadPool;
import com.telegram.bot.csgo.config.properties.BotProperties;
import com.telegram.bot.csgo.processor.UpdateProcessor;
import com.telegram.bot.csgo.service.UpdateProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class BotController extends TelegramLongPollingBot {

    @Lazy
    private final List<UpdateProcessor> updateProcessors;
    private final BotProperties botProperties;
    private final ExecutorService threadPool = newCachedThreadPool();

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

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

}
