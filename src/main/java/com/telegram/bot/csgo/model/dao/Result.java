package com.telegram.bot.csgo.model.dao;

public enum Result {

	ALREADY_EXIST("уже есть в списке"),
	UPDATED("обновлена"),
	INSERTED("добавлена"),
	DELETED("удалена"),
	NOTHING_WAS_CHANGED("Такая запись не найдена"),
	FLAG_NOT_FOUND("Такой флаг не найден"),
	OOPS("Что-то пошло не так...");

	private Result(String message) {
		this.message = message;
	}

	private String message;

	public String getMessage() {
		return message;
	}
}
