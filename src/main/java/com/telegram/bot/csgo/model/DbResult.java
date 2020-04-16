package com.telegram.bot.csgo.model;

public interface DbResult {
	
	String ALREADY_EXIST = "уже есть в списке";
	String UPDATED = "обновлена";
	String INSERTED = "добавлена";
	String DELETED = "удалена";
	String NOTHING_WAS_CHANGED = "Такая запись не найдена";
	String FLAG_NOT_FOUND = "Такой флаг не найден";
	String OOPS = "Что-то пошло не так...";

}
