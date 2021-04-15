package com.telegram.bot.csgo.model.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Result {

	ALREADY_EXIST("уже есть в списке"),
	UPDATED("обновлена"),
	INSERTED("добавлена"),
	DELETED("удалена"),
	NOTHING_WAS_CHANGED("Такая запись не найдена"),
	FLAG_NOT_FOUND("Такой флаг не найден"),
	OOPS("Что-то пошло не так...");

	private String message;

}
