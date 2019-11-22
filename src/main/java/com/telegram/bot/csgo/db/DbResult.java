package com.telegram.bot.csgo.db;

public enum DbResult {
    ALREADY_EXIST("уже есть в списке"),
    UPDATED ("обновлена"),
    INSERTED ("добавлена"),
    DELETED ("удалена"),
    NOTHING_WAS_CHANGED ("Такая запись не найдена"),
    NOT_FOUND ("Такая запись не найдена"),
    OOPS ("Что-то пошло не так...");
    
    private String text;

    public String getText() {
        return text;
    }

    private DbResult(String text) {
        this.text = text;
    }
}
