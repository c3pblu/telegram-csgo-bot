package com.telegram.bot.csgo.processor.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.processor.UpdateProcessor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AdminUpdateProcessor implements UpdateProcessor {

    @Value("${admin.users:463430646}")
    private Integer[] adminUsers;
    private static final String STATS_COMMAND = ".стат";
    private static final String EXEC_COMMAND = ".exec.";
    private BotController botController;

    @Autowired
    public AdminUpdateProcessor(BotController botController) {
        this.botController = botController;
    }

    @Override
    public void process(Update update) {
        stats(update);
        exec(update);
    }

    private void stats(Update update) {
        if (update.getMessage() != null && STATS_COMMAND.equals(update.getMessage().getText())) {
            if (isAdmin(update)) {
                Runtime runtime = Runtime.getRuntime();
                Long free = runtime.freeMemory() / 1024 / 1024;
                Long max = runtime.maxMemory() / 1024 / 1024;
                Long total = runtime.totalMemory() / 1024 / 1024;
                Long used = total - free;
                String message = String.format("JVM Memory Stats (MB), Free: %d, Used: %d, Total: %d, Max: %d", free, used,
                        total, max);
                log.info(message);
                botController.send(SendMessage.builder().chatId(getChatId(update)).text(message).build());
            }
        }
    }

    private void exec(Update update) {
        if (update.getMessage() != null && StringUtils.startsWith(update.getMessage().getText(), EXEC_COMMAND)) {
            if (isAdmin(update)) {
                String command = RegExUtils.removeAll(update.getMessage().getText(), EXEC_COMMAND);
                if (!StringUtils.isBlank(command)) {
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        log.info("Executing command: {}", command);
                        Process proc = runtime.exec(command);
                        proc.waitFor(1, TimeUnit.MINUTES);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        reader.lines().forEach(line -> result.append(line).append("\n"));
                        log.info("Command output: {}", String.valueOf(result));
                        botController
                                .send(SendMessage.builder().chatId(getChatId(update)).text(String.valueOf(result)).build());
                    } catch (Exception e) {
                        log.info(e.getMessage());
                        botController.send(SendMessage.builder().chatId(getChatId(update)).text(e.getMessage()).build());
                    }
                }
            }
        }
    }

    private boolean isAdmin(Update update) {
        if (!Arrays.asList(adminUsers).contains(update.getMessage().getFrom().getId())) {
            botController.send(
                    SendMessage.builder().chatId(getChatId(update)).text("Хм... ты кто такой? За тобой уже выехали!").build());
            return false;
        }
        return true;
    }
}
