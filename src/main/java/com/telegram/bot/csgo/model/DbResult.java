package com.telegram.bot.csgo.model;

public enum DbResult {
    ALREADY_EXIST("уже есть в списке"),
    UPDATED ("обновлена"),
    INSERTED ("добавлена"),
    DELETED ("удалена"),
    NOTHING_WAS_CHANGED ("Такая запись не найдена"),
    FLAG_NOT_FOUND ("Такой флаг не найден"),
    OOPS ("Что-то пошло не так...");
    
    private String text;

    public String getText() {
        return text;
    }

    private DbResult(String text) {
        this.text = text;
    }
}
