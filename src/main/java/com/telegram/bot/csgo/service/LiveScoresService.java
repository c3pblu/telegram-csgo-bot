package com.telegram.bot.csgo.service;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.model.HtmlMessage;
import com.telegram.bot.csgo.repository.EmojiRepository;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class LiveScoresService {

    @Value("${scorebot.endpoints}")
    private String[] endpoints;
    private final Map<String, Socket> sessions = new ConcurrentHashMap<>();
    private final BotController botController;
    private final EmojiRepository emojiRepository;

    @Autowired
    public LiveScoresService(BotController botController, EmojiRepository emojiRepository) {
        this.botController = botController;
        this.emojiRepository = emojiRepository;
    }

    public void start(String chatId, String matchId) {
        stop(chatId, false);
        try {
            int random = ThreadLocalRandom.current().nextInt(endpoints.length);
            String endpoint = endpoints[random];
            Socket socket = IO.socket(endpoint);
            sessions.put(chatId, socket);
            socket.on(Socket.EVENT_CONNECT, args -> socket.emit("readyForMatch", "{token: '', listId: " + matchId + " }"));
            socket.on("log", args -> processLog(chatId, args));
            socket.on("reconnect", args -> socket.emit("readyForMatch", "{token: '', listId: " + matchId + " }"));
            socket.on(Socket.EVENT_DISCONNECT, args -> socket.close());
            socket.connect();
            log.info("Connected to " + endpoint);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void processLog(String chatId, Object... args) {
        JSONObject json = new JSONObject(String.valueOf(args[args.length - 1]));
        if (json.getJSONArray("log").length() > 0) {
            json = json.getJSONArray("log").getJSONObject(0);
            SendMessage message = scorebotMessage(chatId, json);
            if (!StringUtils.isBlank(message.getText())) {
                botController.send(message);
                log.debug("{}", json);
            }
        }
    }

    public void stop(String chatId, boolean sendMessage) {
        if (sessions.containsKey(chatId)) {
            sessions.get(chatId).disconnect();
            sessions.remove(chatId);
            if (sendMessage) {
                botController.send(new HtmlMessage(chatId, "Трансляция остановлена"));
            }
        } else if (sendMessage) {
            botController.send(new HtmlMessage(chatId, "Запущеная трансляция не найдена"));
        }
    }

    public SendMessage scorebotMessage(String chatId, JSONObject json) {
        String diamondOrange = emojiRepository.getEmoji("diamond_orange");
        String diamondBlue = emojiRepository.getEmoji("diamond_blue");
        StringBuilder textMessage = new StringBuilder();
        String logType = json.keys().next();
        log.debug("LogType: {}", logType);
        // Round Start
        if (logType.equals("RoundStart")) {
            textMessage.append("<b>Round Started</b>");
        }
        // Round End
        if (logType.equals("RoundEnd")) {
            String winner = String.valueOf(json.query("/RoundEnd/winner"));
            String winType = String.valueOf(json.query("/RoundEnd/winType"));
            String ctScore = String.valueOf(json.query("/RoundEnd/counterTerroristScore"));
            String tScore = String.valueOf(json.query("/RoundEnd/terroristScore"));
            textMessage.append("<b>Round Over: ");
            if (winner.equals("TERRORIST")) {
                textMessage.append(diamondOrange).append("T");
            } else {
                textMessage.append(diamondBlue).append("CT");
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
            textMessage.append("\n").append(diamondOrange).append(tScore).append(" - ").append(diamondBlue)
                    .append(ctScore).append("</b>");

        }
        // Kill
        if (logType.equals("Kill")) {
            String killerSide = String.valueOf(json.query("/Kill/killerSide"));
            String killerNick = String.valueOf(json.query("/Kill/killerNick"));
            String victimSide = String.valueOf(json.query("/Kill/victimSide"));
            String victimNick = String.valueOf(json.query("/Kill/victimNick"));
            String weapon = String.valueOf(json.query("/Kill/weapon"));
            boolean isHeadShot = (boolean) json.query("/Kill/headShot");
            if (killerSide.equals("TERRORIST")) {
                textMessage.append(diamondOrange);
            } else {
                textMessage.append(diamondBlue);
            }
            textMessage.append("<b>").append(killerNick).append("</b> killed");
            if (victimSide.equals("TERRORIST")) {
                textMessage.append(diamondOrange);
            } else {
                textMessage.append(diamondBlue);
            }
            textMessage.append("<b>").append(victimNick).append("</b> with ").append(weapon);
            if (isHeadShot) {
                textMessage.append(" ").append(emojiRepository.getEmoji("helm"));
            }
        }

        // Bomb Planted
        if (logType.equals("BombPlanted")) {
            String playerNick = String.valueOf(json.query("/BombPlanted/playerNick"));
            String tPlayers = String.valueOf(json.query("/BombPlanted/tPlayers"));
            String ctPlayers = String.valueOf(json.query("/BombPlanted/ctPlayers"));
            textMessage.append(diamondOrange).append("<b>").append(playerNick).append(" ").append(emojiRepository.getEmoji("bomb"))
                    .append(" planted the bomb").append(diamondOrange).append(tPlayers).append(" on")
                    .append(diamondBlue).append(ctPlayers).append("</b>");
        }
        // Bomb Defused
        if (logType.equals("BombDefused")) {
            String playerNick = String.valueOf(json.query("/BombDefused/playerNick"));
            textMessage.append(diamondBlue).append("<b>").append(playerNick).append(" defused the bomb</b>");
        }
        // Suicide
        if (logType.equals("Suicide")) {
            String playerNick = String.valueOf(json.query("/Suicide/playerNick"));
            String side = String.valueOf(json.query("/Suicide/side"));
            if (side.equals("TERRORIST")) {
                textMessage.append(diamondOrange);
            } else {
                textMessage.append(diamondBlue);
            }
            textMessage.append("<b>").append(playerNick).append("</b> committed suicide");
        }
        // PlayerJoin
        if (logType.equals("PlayerJoin")) {
            String playerNick = String.valueOf(json.query("/PlayerJoin/playerNick"));
            textMessage.append("<b>").append(playerNick).append("</b> joined the game");
        }
        // PlayerQuit
        if (logType.equals("PlayerQuit")) {
            String playerNick = String.valueOf(json.query("/PlayerQuit/playerNick"));
            String playerSide = String.valueOf(json.query("/PlayerQuit/playerSide"));
            if (playerSide.equals("TERRORIST")) {
                textMessage.append(diamondOrange);
            }
            if (playerSide.equals("CT")) {
                textMessage.append(diamondBlue);
            }
            textMessage.append("<b>").append(playerNick).append("</b> quit the game");
        }
        // MatchStarted
        if (logType.equals("MatchStarted")) {
            String map = String.valueOf(json.query("/MatchStarted/map"));
            textMessage.append("<b>Match Started: ").append(map).append("</b>");
        }
        log.debug("Log Message: {}", textMessage);
        return new HtmlMessage(chatId, textMessage);
    }

}
