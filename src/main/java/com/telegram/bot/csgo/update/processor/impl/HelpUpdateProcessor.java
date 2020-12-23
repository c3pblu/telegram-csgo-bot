package com.telegram.bot.csgo.update.processor.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.update.processor.UpdateProcessor;

@Component
public class HelpUpdateProcessor implements UpdateProcessor {

	@Value("${help.message.file:#{null}}")
	private String helpFile;

	private BotController botController;

	@Autowired
	public HelpUpdateProcessor(BotController botController) {
		this.botController = botController;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(HelpUpdateProcessor.class);
	private static final String HELP = ".хелп";

	@Override
	public void process(Update update) {
		if (update.hasMessage() && HELP.equalsIgnoreCase(update.getMessage().getText())) {
			botController.send(helpMessage(getChatId(update)));
		}
	}

	private SendMessage helpMessage(String chatId) {
		return SendMessage.builder().parseMode("markdown").text(helpText()).chatId(chatId).build();
	}

	private String helpText() {
		String helpMessage = "Не найден файл описания помощи!";
		if (helpFile == null) {
			return helpMessage;
		}
		try {
			List<String> lines = Files.readAllLines(Paths.get(helpFile), StandardCharsets.UTF_8);
			helpMessage = String.join(System.lineSeparator(), lines);
		} catch (IOException e) {
			LOGGER.error("File for Help message not found! See 'help.message.file' property");
		}
		return helpMessage;
	}
}
