package com.telegram.bot.csgo.processor;

import javax.annotation.PostConstruct;
import java.io.IOException;
import static com.telegram.bot.csgo.helper.CommandHelper.HELP_COMMAND;
import static com.telegram.bot.csgo.helper.CommandHelper.START_COMMAND;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.telegram.telegrambots.meta.api.methods.ParseMode.MARKDOWN;
import static org.telegram.telegrambots.meta.api.methods.send.SendMessage.builder;
import com.telegram.bot.csgo.controller.BotController;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class HelpUpdateProcessor extends UpdateProcessor {

    private static final String HELP_FILE_PATH = "help/message";

    private final BotController botController;
    private String helpText;

    @PostConstruct
    void init() {
        try {
            helpText = new String(
                    new ClassPathResource(HELP_FILE_PATH).getInputStream().readAllBytes(), UTF_8);
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }

    @Override
    public void process(@NonNull Update update) {
        if (isHelpCommand(update)) {
            botController.send(helpMessage(getChatId(update)));
        }
    }

    private SendMessage helpMessage(String chatId) {
        return builder()
                .parseMode(MARKDOWN)
                .text(helpText)
                .chatId(chatId)
                .build();
    }

    private static boolean isHelpCommand(Update update) {
        return update.hasMessage()
                && (HELP_COMMAND.equalsIgnoreCase(update.getMessage().getText())
                || START_COMMAND.equalsIgnoreCase(update.getMessage().getText()));
    }
}
