package com.telegram.bot.csgo.processor;

import com.telegram.bot.csgo.controller.BotController;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static java.lang.String.valueOf;

public abstract class UpdateProcessor {

    public abstract void process(@NonNull Update update);

    protected void deleteMenu(BotController botController, Update update) {
        if (update.hasCallbackQuery()) {
            botController.send(new DeleteMessage(valueOf(update.getCallbackQuery().getMessage().getChatId()),
                    update.getCallbackQuery().getMessage().getMessageId()));
        }
    }

    @Nullable
    protected String getChatId(@NonNull Update update) {
        if (update.hasMessage()) {
            return valueOf(update.getMessage().getChatId());
        }
        if (update.hasCallbackQuery()) {
            return valueOf(update.getCallbackQuery().getMessage().getChatId());
        }
        return null;
    }
}
