package com.telegram.bot.csgo.service;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.telegram.bot.csgo.controller.BotController;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

@Service
public class WebSocketService {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketService.class);
	private Map<Long, Socket> sessions = new HashMap<>();

	@Value("${scorebot.endpoints}")
	private String[] endpoints;

	private MessageService messageService;
	private BotController botController;

	@Autowired
	WebSocketService(MessageService messageService, @Lazy BotController botController) {
		this.messageService = messageService;
		this.botController = botController;
	}

	public void live(Long chatId, String matchId) {
		stop(chatId);
		try {
			int random = ThreadLocalRandom.current().nextInt(endpoints.length);
			String endpoint = endpoints[random];
			Socket socket = IO.socket(endpoint);
			sessions.put(chatId, socket);
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					socket.emit("readyForMatch", "{token: '', listId: " + matchId + " }");
				}
			});

			socket.on("log", new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					JSONObject json = new JSONObject(args[args.length - 1].toString());
					if (json.getJSONArray("log").length() > 0) {
						json = json.getJSONArray("log").getJSONObject(0);
						SendMessage message = messageService.scorebot(json);
						if (!StringUtils.isBlank(message.getText())) {
							botController.sendMessage(chatId, message);
							LOGGER.debug("{}", json);
						}
					}
				}
			});

			socket.on("reconnect", new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					socket.emit("readyForMatch", "{token: '', listId: " + matchId + " }");
				}
			});

			socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					socket.close();
				}

			});

			socket.connect();
			LOGGER.info("Connected to " + endpoint);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public void stop(Long chatId) {
		if (sessions.containsKey(chatId)) {
			sessions.get(chatId).disconnect();
		}
	}

}
