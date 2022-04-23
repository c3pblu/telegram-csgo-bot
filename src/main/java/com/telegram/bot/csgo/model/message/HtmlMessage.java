package com.telegram.bot.csgo.model.message;

import static org.telegram.telegrambots.meta.api.methods.ParseMode.HTML;
import lombok.Builder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public final class HtmlMessage extends SendMessage {

    @Builder(builderMethodName = "htmlBuilder")
    public HtmlMessage(String chatId, Object text) {
        super(chatId, String.valueOf(text));
        setDisableWebPagePreview(true);
        setDisableNotification(true);
        setParseMode(HTML);
    }
}
