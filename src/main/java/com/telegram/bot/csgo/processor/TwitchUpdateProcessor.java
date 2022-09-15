package com.telegram.bot.csgo.processor;

import static com.telegram.bot.csgo.helper.CommandHelper.STREAMS_NEXT_PAGE_CALLBACK;
import static com.telegram.bot.csgo.helper.CommandHelper.STREAMS_COMMAND;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.TwitchService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TwitchUpdateProcessor extends UpdateProcessor {

    private final BotController botController;
    private final TwitchService twitchService;

    @Override
    public void process(@NonNull Update update) {
        var chatId = getChatId(update);
        if (isTwitchCommand(update)) {
            send(update, chatId, false);
        }
        if (isTwitchCallback(update)) {
            send(update, chatId, true);
        }
    }

    private void send(Update update, String chatId, boolean isNextPage) {
        botController.send(twitchService.getStreams(chatId, isNextPage));
        botController.send(twitchService.nextPage(chatId));
        deleteMenu(botController, update);
    }

    private static boolean isTwitchCommand(Update update) {
        return (update.hasMessage() && STREAMS_COMMAND.equalsIgnoreCase(update.getMessage().getText()))
                || (update.hasCallbackQuery() && STREAMS_COMMAND.equals(update.getCallbackQuery().getData()));
    }

    private static boolean isTwitchCallback(Update update) {
        return update.hasCallbackQuery() && STREAMS_NEXT_PAGE_CALLBACK.equals(update.getCallbackQuery().getData());
    }

}
