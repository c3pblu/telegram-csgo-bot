package com.telegram.bot.csgo.controller;

import java.time.Instant;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.CallBackData;
import com.telegram.bot.csgo.model.Commands;
import com.telegram.bot.csgo.service.HttpService;
import com.telegram.bot.csgo.service.MessageService;
import com.telegram.bot.csgo.service.TwitchService;
import com.telegram.bot.csgo.service.WebSocketService;

@Service
public class BotController extends TelegramLongPollingBot {

	private static final String TEAM_REGEXP = "\\.[к][о][м][а][н][д][ы]";
	private static final String NAME_REGEXP = "([\\w]*\\s{0,}\\.{0,})*";
	private static final String COUNTRY_REGEXP = "\\[[A-Z][A-Z]\\]";
	private static final String PLUS_REGEXP = "\\+";
	private static final String MINUS_REGEXP = "\\-";
	private static final String START_REGEXP = "\\.[с][т][а][р][т]\\-\\d*";
	private final static String HLTV = "https://www.hltv.org";

	private MessageService messageService;
	private HttpService httpService;
	private TwitchService twitchService;
	private WebSocketService webSocketService;
	private Dao dao;
	

	@Autowired
	BotController(MessageService message, HttpService httpService, TwitchService twitchService, WebSocketService webSocketService, Dao dao) {
		this.messageService = message;
		this.httpService = httpService;
		this.twitchService = twitchService;
		this.webSocketService = webSocketService;
		this.dao = dao;
	}

	@Value(value = "${bot.callback.timeout}")
	private Long callBackTimeout;
	@Value(value = "${bot.name}")
	private String botName;
	@Value(value = "${bot.token}")
	private String botToken;
	@Value(value = "${bot.scheduler.chat.id}")
	private Long schedulerChatId;
	@Value(value = "${bot.message.timeout}")
	private Long messageTimeout;
	@Value(value = "${bot.message.uniq.count}")
	private Integer uniqCount;

	@Override
	public String getBotUsername() {
		return botName;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
	public void onUpdateReceived(Update update) {
		// We check if the update has a message and the message has text
		if (update.hasMessage() && update.getMessage().hasText()) {
			// If message timeout - skip message
			if (isTimeout(update)) {
				return;
			}
			Long chatId = update.getMessage().getChatId();
			String text = update.getMessage().getText();
			// Help
			if (text.equalsIgnoreCase(Commands.HELP)) {
				sendMessage(chatId, messageService.help());
			}
			// Menu
			else if (text.equalsIgnoreCase(Commands.MENU)) {
				sendMessage(chatId, messageService.menu());
			}
			// Matches
			else if (text.equalsIgnoreCase(Commands.MATCHES)) {
				Document doc = httpService.getDocument(HLTV + "/matches");
				sendMessage(chatId, messageService.matches(doc, chatId));
			}
			// Results
			else if (text.equalsIgnoreCase(Commands.RESULTS)) {
				Document doc = httpService.getDocument(HLTV + "/results");
				sendMessage(chatId, messageService.results(doc, chatId));
			}
			// Top Players
			else if (text.equalsIgnoreCase(Commands.TOP_10_PLAYERS)
					|| text.equalsIgnoreCase(Commands.TOP_20_PLAYERS)
					|| text.equalsIgnoreCase(Commands.TOP_30_PLAYERS)) {
				Integer count = Integer.parseInt(text.substring(4, 6));
				topPlayers(chatId, count);
			}
			// Top Teams
			else if (text.equalsIgnoreCase(Commands.TOP_10)
					|| text.equalsIgnoreCase(Commands.TOP_20)
					|| text.equalsIgnoreCase(Commands.TOP_30)) {
				Integer count = Integer.parseInt(update.getMessage().getText().substring(4));
				topTeams(chatId, count);
			}
			// Twitch streams
			else if (text.equalsIgnoreCase(Commands.STREAMS)) {
				sendMessage(chatId, messageService.streams(twitchService.getStreams(chatId, false)));
				sendMessage(chatId, messageService.nextPage());
			}
			// Favorite Teams
			else if (text.equalsIgnoreCase(Commands.TEAMS)) {
				sendMessage(chatId, messageService.favoriteTeams(dao.getTeams(chatId), chatId));
			}

			else if (text.matches(TEAM_REGEXP + ".*")) {
				// Insert/Update (.команды+)
				if (text.matches(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP + COUNTRY_REGEXP)) {
					String team = text.replaceAll("\\[..\\]", "").replaceAll(TEAM_REGEXP + PLUS_REGEXP, "").trim();
					String countryCode = text.replaceAll(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP, "")
							.replaceAll("\\[", "").replaceAll("\\]", "");
					String dbResult = dao.updateOrSaveTeam(chatId, team, countryCode);
					sendMessage(chatId, messageService.dbResult(dbResult, team));
				}
				// Delete (.команды-)
				else if (text.matches(TEAM_REGEXP + MINUS_REGEXP + NAME_REGEXP)) {
					String team = text.replaceAll(TEAM_REGEXP + MINUS_REGEXP, "").trim();
					String dbResult = dao.deleteTeam(chatId, team);
					sendMessage(chatId, messageService.dbResult(dbResult, team));
				} else {
					sendMessage(chatId, messageService.teamsFormat());
				}
			}
			// ScoreBot
			else if (text.equals(Commands.SCOREBOT)) {
				sendMessage(chatId, messageService.scorebot());
			}
			else if (text.matches(START_REGEXP)) {
				webSocketService.live(chatId, text.substring(7));
			}
			else if (text.equalsIgnoreCase(Commands.STOP)) {
				webSocketService.stop(chatId);
				sendMessage(chatId, messageService.stoped());
			}
			// Private and mentioned message
			else if (StringUtils.startsWith(update.getMessage().getText(), "@" + botName)
					|| update.getMessage().getChat().isUserChat()) {
				sendMessage(chatId, messageService.sticker(chatId, uniqCount));
				Document doc = httpService.getDocument("https://api.forismatic.com/api/1.0/?method=getQuote&format=html&lang=ru");
				sendMessage(chatId, messageService.cite(doc));
			}
		}

		// Check call back from Menu
		if (update.getCallbackQuery() != null) {
			Long chatId = update.getCallbackQuery().getMessage().getChatId();
			CallbackQuery callBack = update.getCallbackQuery();
			if (isTimeout(callBack)) {
				sendMessage(chatId, messageService.oops());
				deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
				return;
			}
			String data = callBack.getData();
			if (data.equals(CallBackData.TOP_10)) {
				topTeams(chatId, 10);
			}
			if (data.equals(CallBackData.TOP_20)) {
				topTeams(chatId, 20);
			}
			if (data.equals(CallBackData.TOP_30)) {
				topTeams(chatId, 30);
			}
			if (data.equals(CallBackData.TOP_10_PLAYERS)) {
				topPlayers(chatId, 10);
			}
			if (data.equals(CallBackData.TOP_20_PLAYERS)) {
				topPlayers(chatId, 20);
			}
			if (data.equals(CallBackData.TOP_30_PLAYERS)) {
				topPlayers(chatId, 30);
			}
			if (data.equals(CallBackData.MATCHES)) {
				Document doc = httpService.getDocument(HLTV + "/matches");
				sendMessage(chatId, messageService.matches(doc, chatId));
			}
			if (data.equals(CallBackData.RESULTS)) {
				Document doc = httpService.getDocument(HLTV + "/results");
				sendMessage(chatId, messageService.results(doc, chatId));
			}
			if (data.equals(CallBackData.TEAMS)) {
				sendMessage(chatId, messageService.favoriteTeams(dao.getTeams(chatId), chatId));
			}
			if (data.equals(CallBackData.STREAMS)) {
				sendMessage(chatId, messageService.streams(twitchService.getStreams(chatId, false)));
				sendMessage(chatId, messageService.nextPage());
			}
			if (data.equals(CallBackData.NEXT_PAGE)) {
				sendMessage(chatId, messageService.streams(twitchService.getStreams(chatId, true)));
				sendMessage(chatId, messageService.nextPage());
			}
			if (data.equals(CallBackData.SCOREBOT)) {
				sendMessage(chatId, messageService.scorebot());
			}

			deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
		}
	}

	private void topPlayers(Long chatId, int count) {
		String year = String.valueOf(LocalDate.now().getYear());
		Document doc = httpService
				.getDocument(HLTV + "/stats/players?startDate=" + year + "-01-01&endDate=" + year + "-12-31");
		sendMessage(chatId, messageService.topPlayers(doc, count));
	}

	private void topTeams(Long chatId, int count) {
		Document doc = httpService.getDocument(HLTV + "/ranking/teams");
		sendMessage(chatId, messageService.topTeams(doc, count));
	}

	public void sendMessage(Long chatId, SendMessage msg) {
		msg.setChatId(chatId);
		try {
			execute(msg); // Call method to send the message
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(Long chatId, SendSticker msg) {
		msg.setChatId(chatId);
		try {
			execute(msg); // Call method to send the message
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private void deleteMessage(Long chatId, Integer msgId) {
		try {
			execute(new DeleteMessage(chatId, msgId));
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	private boolean isTimeout(Object obj) {
		Update update = null;
		CallbackQuery callBack = null;

		if (obj instanceof Update) {
			update = (Update) obj;
		}

		if (obj instanceof CallbackQuery) {
			callBack = (CallbackQuery) obj;
		}

		if (update != null) {
			long responseTime = Instant.now().getEpochSecond() - update.getMessage().getDate();
			if (responseTime > messageTimeout) {
				return true;
			}
		}

		if (callBack != null) {
			long responseTime = Instant.now().getEpochSecond() - callBack.getMessage().getDate();
			if (responseTime > callBackTimeout) {
				return true;
			}
		}
		return false;
	}

	@Scheduled(cron = "${bot.scheduler.matches.cron}")
	private void todayMatchesScheduler() {
		sendMessage(schedulerChatId, messageService.matchesForToday());
		Document doc = httpService.getDocument(HLTV + "/matches");
		sendMessage(schedulerChatId, messageService.matches(doc, schedulerChatId));
	}

	@Scheduled(cron = "${bot.scheduler.results.cron}")
	private void todayResultsScheduler() {
		sendMessage(schedulerChatId, messageService.resultsForToday());
		Document doc = httpService.getDocument(HLTV + "/results");
		sendMessage(schedulerChatId, messageService.results(doc, schedulerChatId));
	}

}
