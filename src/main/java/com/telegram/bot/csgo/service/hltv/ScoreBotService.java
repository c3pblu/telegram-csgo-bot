package com.telegram.bot.csgo.service.hltv;

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
import com.telegram.bot.csgo.model.Emoji;
import com.telegram.bot.csgo.service.MessageService;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

@Service
public class ScoreBotService {

	@Value("${scorebot.endpoints}")
	private String[] endpoints;

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);
	private Map<Long, Socket> sessions = new HashMap<>();

	private MessageService messageService;
	private BotController botController;

	@Autowired
	public ScoreBotService(MessageService messageService, @Lazy BotController botController) {
		this.messageService = messageService;
		this.botController = botController;
	}

	public SendMessage scorebot(JSONObject json) {
		StringBuilder textMessage = new StringBuilder();
		String logType = json.keys().next();
		LOGGER.debug("LogType: {}", logType);
		// Round Start
		if (logType.equals("RoundStart")) {
			textMessage.append("<b>Round Started</b>");
		}
		// Round End
		if (logType.equals("RoundEnd")) {
			String winner = json.query("/RoundEnd/winner").toString();
			String winType = json.query("/RoundEnd/winType").toString();
			String ctScore = json.query("/RoundEnd/counterTerroristScore").toString();
			String tScore = json.query("/RoundEnd/terroristScore").toString();
			textMessage.append("<b>Round Over: ");
			if (winner.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE).append("T");
			} else {
				textMessage.append(Emoji.DIMOND_BLUE).append("CT");
			}
			textMessage.append(" Win - ");
			switch (winType) {
			case "Target_Bombed":
				textMessage.append("Target Bombed");
				break;
			case "Bomb_Defused":
				textMessage.append("Bomb Defused");
				break;
			case "Target_Saved":
				textMessage.append("Target Saved");
				break;
			default:
				textMessage.append("Enemy Eliminated");
				break;
			}
			textMessage.append("\n").append(Emoji.DIMOND_ORANGE).append(tScore).append(" - ").append(Emoji.DIMOND_BLUE)
					.append(ctScore).append("</b>");

		}
		// Kill
		if (logType.equals("Kill")) {
			String killerSide = json.query("/Kill/killerSide").toString();
			String killerNick = json.query("/Kill/killerNick").toString();
			String victimSide = json.query("/Kill/victimSide").toString();
			String victimNick = json.query("/Kill/victimNick").toString();
			String weapon = json.query("/Kill/weapon").toString();
			boolean isHeadShot = (boolean) json.query("/Kill/headShot");
			if (killerSide.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE);
			} else {
				textMessage.append(Emoji.DIMOND_BLUE);
			}
			textMessage.append("<b>").append(killerNick).append("</b> killed");
			if (victimSide.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE);
			} else {
				textMessage.append(Emoji.DIMOND_BLUE);
			}
			textMessage.append("<b>").append(victimNick).append("</b> with ").append(weapon);
			if (isHeadShot) {
				textMessage.append(" ").append(Emoji.HELM);
			}
		}

		// Bomb Planted
		if (logType.equals("BombPlanted")) {
			String playerNick = json.query("/BombPlanted/playerNick").toString();
			String tPlayers = json.query("/BombPlanted/tPlayers").toString();
			String ctPlayers = json.query("/BombPlanted/ctPlayers").toString();
			textMessage.append(Emoji.DIMOND_ORANGE).append("<b>").append(playerNick).append(" ").append(Emoji.BOMB)
					.append(" planted the bomb").append(Emoji.DIMOND_ORANGE).append(tPlayers).append(" on")
					.append(Emoji.DIMOND_BLUE).append(ctPlayers).append("</b>");
		}
		// Bomb Defused
		if (logType.equals("BombDefused")) {
			String playerNick = json.query("/BombDefused/playerNick").toString();
			textMessage.append(Emoji.DIMOND_BLUE).append("<b>").append(playerNick).append(" defused the bomb</b>");
		}
		// Suicide
		if (logType.equals("Suicide")) {
			String playerNick = json.query("/Suicide/playerNick").toString();
			String side = json.query("/Suicide/side").toString();
			if (side.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE);
			} else {
				textMessage.append(Emoji.DIMOND_BLUE);
			}
			textMessage.append("<b>").append(playerNick).append("</b> committed suicide");
		}
		// PlayerJoin
		if (logType.equals("PlayerJoin")) {
			String playerNick = json.query("/PlayerJoin/playerNick").toString();
			textMessage.append("<b>").append(playerNick).append("</b> joined the game");
		}
		// PlayerQuit
		if (logType.equals("PlayerQuit")) {
			String playerNick = json.query("/PlayerQuit/playerNick").toString();
			String playerSide = json.query("/PlayerQuit/playerSide").toString();
			if (playerSide.equals("TERRORIST")) {
				textMessage.append(Emoji.DIMOND_ORANGE);
			}
			if (playerSide.equals("CT")) {
				textMessage.append(Emoji.DIMOND_BLUE);
			}
			textMessage.append("<b>").append(playerNick).append("</b> quit the game");
		}
		// MatchStarted
		if (logType.equals("MatchStarted")) {
			String map = json.query("/MatchStarted/map").toString();
			textMessage.append("<b>Match Started: ").append(map).append("</b>");
		}
		LOGGER.debug("Log Message: {}", textMessage);
		return messageService.text(textMessage.toString());
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
						SendMessage message = scorebot(json);
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

	public SendMessage scorebot() {
		return messageService.text(Emoji.INFO
				+ " Для запуска трансляции:\n.<b>старт-1234567</b> (где 1234567 это Match ID - его можно посмотреть в .мачти)\n<b>.стоп</b> - остановить трансяцию");
	}

}
