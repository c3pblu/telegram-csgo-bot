package com.telegram.bot.csgo.processor;

import javax.annotation.Nullable;
import static java.lang.String.valueOf;
import com.telegram.bot.csgo.controller.BotController;
import lombok.NonNull;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProcessor {

    void process(@NonNull Update update);

    default void deleteMenu(BotController botController, Update update) {
        if (update.hasCallbackQuery()) {
            botController.send(new DeleteMessage(valueOf(update.getCallbackQuery().getMessage().getChatId()),
                    update.getCallbackQuery().getMessage().getMessageId()));
        }
    }

    @Nullable
    default String getChatId(@NonNull Update update) {
        if (update.hasMessage()) {
            return valueOf(update.getMessage().getChatId());
        }
        if (update.hasCallbackQuery()) {
            return valueOf(update.getCallbackQuery().getMessage().getChatId());
        }
        return null;
    }
}
