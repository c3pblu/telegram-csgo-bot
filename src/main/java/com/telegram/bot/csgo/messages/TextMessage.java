package com.telegram.bot.csgo.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class TextMessage extends SendMessage {

    private final static String HTML = "html";

    public TextMessage(Object msg) {
        this.disableNotification();
        this.disableWebPagePreview();
        this.setParseMode(HTML);
        this.setText(msg.toString());
    }

}
