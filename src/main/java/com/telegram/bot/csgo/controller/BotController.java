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

import com.telegram.bot.csgo.model.CallBackData;
import com.telegram.bot.csgo.model.Commands;
import com.telegram.bot.csgo.service.HelpService;
import com.telegram.bot.csgo.service.MenuService;
import com.telegram.bot.csgo.service.MessageService;
import com.telegram.bot.csgo.service.hltv.MatchesService;
import com.telegram.bot.csgo.service.hltv.ResultsService;
import com.telegram.bot.csgo.service.hltv.ScoreBotService;
import com.telegram.bot.csgo.service.hltv.TopPlayersService;
import com.telegram.bot.csgo.service.hltv.TopTeamsService;
import com.telegram.bot.csgo.service.http.HttpService;
import com.telegram.bot.csgo.service.teams.FavoriteTeamsService;
import com.telegram.bot.csgo.service.twitch.TwitchService;

@Service
public class BotController extends TelegramLongPollingBot {

	private static final String TEAM_REGEXP = "\\.[к][о][м][а][н][д][ы]";
	private static final String NAME_REGEXP = "([\\w]*\\s{0,}\\.{0,})*";
	private static final String COUNTRY_REGEXP = "\\[[A-Z][A-Z]\\]";
	private static final String PLUS_REGEXP = "\\+";
	private static final String MINUS_REGEXP = "\\-";
	private static final String START_REGEXP = "\\.[с][т][а][р][т]\\-\\d*";
	private final static String HLTV = "https://www.hltv.org";

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
	
	private MessageService messageHelper;
	private HttpService httpService;
	private TwitchService twitchService;
	private HelpService helpService;
	private MenuService menuService;
	private MatchesService matchesService;
	private ResultsService resultsService;
	private FavoriteTeamsService favoriteTeamsService;
	private TopTeamsService topTeamsService;
	private TopPlayersService topPlayersService;
	private ScoreBotService scoreBotService;

	@Autowired
	public BotController(MessageService messageHelper, HttpService httpService, TwitchService twitchService,
			HelpService helpService, MenuService menuService, MatchesService matchesService,
			ResultsService resultsService, FavoriteTeamsService favoriteTeamsService, TopTeamsService topTeamsService,
			TopPlayersService topPlayersService, ScoreBotService scoreBotService) {
		this.messageHelper = messageHelper;
		this.httpService = httpService;
		this.twitchService = twitchService;
		this.helpService = helpService;
		this.menuService = menuService;
		this.matchesService = matchesService;
		this.resultsService = resultsService;
		this.favoriteTeamsService = favoriteTeamsService;
		this.topTeamsService = topTeamsService;
		this.topPlayersService = topPlayersService;
		this.scoreBotService = scoreBotService;
	}

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
				sendMessage(chatId, helpService.help());
			}
			// Menu
			else if (text.equalsIgnoreCase(Commands.MENU)) {
				sendMessage(chatId, menuService.menu());
			}
			// Matches
			else if (text.equalsIgnoreCase(Commands.MATCHES)) {
				Document doc = httpService.getDocument(HLTV + "/matches");
				sendMessage(chatId, matchesService.matches(doc, chatId));
			}
			// Results
			else if (text.equalsIgnoreCase(Commands.RESULTS)) {
				Document doc = httpService.getDocument(HLTV + "/results");
				sendMessage(chatId, resultsService.results(doc, chatId));
			}
			// Top Players
			else if (text.equalsIgnoreCase(Commands.TOP_10_PLAYERS) || text.equalsIgnoreCase(Commands.TOP_20_PLAYERS)
					|| text.equalsIgnoreCase(Commands.TOP_30_PLAYERS)) {
				Integer count = Integer.parseInt(text.substring(4, 6));
				topPlayers(chatId, count);
			}
			// Top Teams
			else if (text.equalsIgnoreCase(Commands.TOP_10) || text.equalsIgnoreCase(Commands.TOP_20)
					|| text.equalsIgnoreCase(Commands.TOP_30)) {
				Integer count = Integer.parseInt(update.getMessage().getText().substring(4));
				topTeams(chatId, count);
			}
			// Twitch streams
			else if (text.equalsIgnoreCase(Commands.STREAMS)) {
				sendMessage(chatId, twitchService.getStreams(chatId, false));
				sendMessage(chatId, twitchService.nextPage());
			}
			// Favorite Teams
			else if (text.equalsIgnoreCase(Commands.TEAMS)) {
				sendMessage(chatId, favoriteTeamsService.favoriteTeams(chatId));
			}

			else if (text.matches(TEAM_REGEXP + ".*")) {
				// Insert/Update (.команды+)
				if (text.matches(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP + COUNTRY_REGEXP)) {
					String team = text.replaceAll("\\[..\\]", "").replaceAll(TEAM_REGEXP + PLUS_REGEXP, "").trim();
					String countryCode = text.replaceAll(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP, "")
							.replaceAll("\\[", "").replaceAll("\\]", "");
					String dbResult = favoriteTeamsService.updateOrSaveTeam(chatId, team, countryCode);
					sendMessage(chatId, messageHelper.dbResult(dbResult, team));
				}
				// Delete (.команды-)
				else if (text.matches(TEAM_REGEXP + MINUS_REGEXP + NAME_REGEXP)) {
					String team = text.replaceAll(TEAM_REGEXP + MINUS_REGEXP, "").trim();
					String dbResult = favoriteTeamsService.deleteTeam(chatId, team);
					sendMessage(chatId, messageHelper.dbResult(dbResult, team));
				} else {
					sendMessage(chatId, messageHelper.teamsFormat());
				}
			}
			// ScoreBot
			else if (text.equals(Commands.SCOREBOT)) {
				sendMessage(chatId, scoreBotService.scorebot());
			} else if (text.matches(START_REGEXP)) {
				scoreBotService.live(chatId, text.substring(7));
			} else if (text.equalsIgnoreCase(Commands.STOP)) {
				scoreBotService.stop(chatId);
				sendMessage(chatId, messageHelper.stoped());
			}
			// Private and mentioned message
			else if (StringUtils.startsWith(update.getMessage().getText(), "@" + botName)
					|| update.getMessage().getChat().isUserChat()) {
				sendMessage(chatId, messageHelper.sticker(chatId, uniqCount));
				Document doc = httpService
						.getDocument("https://api.forismatic.com/api/1.0/?method=getQuote&format=html&lang=ru");
				sendMessage(chatId, messageHelper.cite(doc));
			}
		}

		// Check call back from Menu
		if (update.getCallbackQuery() != null) {
			Long chatId = update.getCallbackQuery().getMessage().getChatId();
			CallbackQuery callBack = update.getCallbackQuery();
			if (isTimeout(callBack)) {
				sendMessage(chatId, messageHelper.oops());
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
				sendMessage(chatId, matchesService.matches(doc, chatId));
			}
			if (data.equals(CallBackData.RESULTS)) {
				Document doc = httpService.getDocument(HLTV + "/results");
				sendMessage(chatId, resultsService.results(doc, chatId));
			}
			if (data.equals(CallBackData.TEAMS)) {
				sendMessage(chatId, favoriteTeamsService.favoriteTeams(chatId));
			}
			if (data.equals(CallBackData.STREAMS)) {
				sendMessage(chatId, twitchService.getStreams(chatId, false));
				sendMessage(chatId, twitchService.nextPage());
			}
			if (data.equals(CallBackData.NEXT_PAGE)) {
				sendMessage(chatId, twitchService.getStreams(chatId, true));
				sendMessage(chatId, twitchService.nextPage());
			}
			if (data.equals(CallBackData.SCOREBOT)) {
				sendMessage(chatId, scoreBotService.scorebot());
			}

			deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
		}
	}

	private void topPlayers(Long chatId, int count) {
		String year = String.valueOf(LocalDate.now().getYear());
		Document doc = httpService
				.getDocument(HLTV + "/stats/players?startDate=" + year + "-01-01&endDate=" + year + "-12-31");
		sendMessage(chatId, topPlayersService.topPlayers(doc, count));
	}

	private void topTeams(Long chatId, int count) {
		Document doc = httpService.getDocument(HLTV + "/ranking/teams");
		sendMessage(chatId, topTeamsService.topTeams(doc, count));
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
		sendMessage(schedulerChatId, matchesService.matchesForToday());
		Document doc = httpService.getDocument(HLTV + "/matches");
		sendMessage(schedulerChatId, matchesService.matches(doc, schedulerChatId));
	}

	@Scheduled(cron = "${bot.scheduler.results.cron}")
	private void todayResultsScheduler() {
		sendMessage(schedulerChatId, resultsService.resultsForToday());
		Document doc = httpService.getDocument(HLTV + "/results");
		sendMessage(schedulerChatId, resultsService.results(doc, schedulerChatId));
	}

}
