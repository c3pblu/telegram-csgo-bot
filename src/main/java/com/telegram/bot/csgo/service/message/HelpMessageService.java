package com.telegram.bot.csgo.service.message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.model.SendMessageBuilder;

@Service
public class HelpMessageService {

	private static final Logger LOGGER = LoggerFactory.getLogger(HelpMessageService.class);

	@Value("${help.message.file:#{null}}")
	private String helpFile;

	public String helpText() {
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

	public SendMessage help() {
		return new SendMessageBuilder().parseMode("markdown").text(helpText()).build();
	}

}
