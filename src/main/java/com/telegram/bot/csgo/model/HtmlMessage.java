package com.telegram.bot.csgo.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class HtmlMessage extends SendMessage {

    public static HtmlMessageBuilder httpBuilder() {
        return new HtmlMessageBuilder();
    }

    public HtmlMessage(String chatId, Object text) {
        setChatId(chatId);
        setText(String.valueOf(text));
        setDisableWebPagePreview(true);
        setDisableNotification(true);
        setParseMode("html");
    }

    public static class HtmlMessageBuilder {
        private String chatId;
        private Object text;

        public HtmlMessageBuilder setChatId(String chatId) {
            this.chatId = chatId;
            return this;
        }

        public HtmlMessageBuilder setText(Object text) {
            this.text = text;
            return this;
        }

        public HtmlMessage build() {
            return new HtmlMessage(chatId, text);
        }
    }

}
