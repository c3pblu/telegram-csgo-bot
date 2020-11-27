package com.telegram.bot.csgo.service;

import java.time.Instant;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.model.CallBackData;
import com.telegram.bot.csgo.model.Commands;
import com.telegram.bot.csgo.service.hltv.HltvService;
import com.telegram.bot.csgo.service.http.HttpService;
import com.telegram.bot.csgo.service.message.MessageService;
import com.telegram.bot.csgo.service.teams.FavoriteTeamsService;
import com.telegram.bot.csgo.service.twitch.TwitchService;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BotService implements Runnable {

	private static final String TEAM_REGEXP = "\\.[к][о][м][а][н][д][ы]";
	private static final String NAME_REGEXP = "([\\w]*\\s{0,}\\.{0,})*";
	private static final String COUNTRY_REGEXP = "\\[[A-Z][A-Z]\\]";
	private static final String PLUS_REGEXP = "\\+";
	private static final String MINUS_REGEXP = "\\-";
	private static final String START_REGEXP = "\\.[с][т][а][р][т]\\-\\d*";
	private final static String HLTV = "https://www.hltv.org";

	@Value(value = "${bot.callback.timeout}")
	private Long callBackTimeout;
	@Value(value = "${bot.message.timeout}")
	private Long messageTimeout;
	@Value(value = "${bot.message.uniq.count}")
	private Integer uniqCount;

	private MessageService messageHelper;
	private HttpService httpService;
	private TwitchService twitchService;
	private FavoriteTeamsService favoriteTeamsService;
	private BotController botController;
	private HltvService hltvService;
	private Update update;

	@Autowired
	public BotService(MessageService messageHelper, HttpService httpService, TwitchService twitchService,
			FavoriteTeamsService favoriteTeamsService, BotController botController, HltvService hltvService) {
		this.messageHelper = messageHelper;
		this.httpService = httpService;
		this.twitchService = twitchService;
		this.favoriteTeamsService = favoriteTeamsService;
		this.botController = botController;
		this.hltvService = hltvService;
	}

	@Override
	public void run() {
		// We check if the update has a message and the message has text
		if (update.hasMessage() && update.getMessage().hasText()) {
			// If message timeout - skip message
			if (isTimeout(update)) {
				return;
			}
			String chatId = String.valueOf(update.getMessage().getChatId());
			String text = update.getMessage().getText();
			// Help
			if (text.equalsIgnoreCase(Commands.HELP)) {
				botController.sendMessage(messageHelper.helpMessage(chatId));
			}
			// Menu
			else if (text.equalsIgnoreCase(Commands.MENU)) {
				botController.sendMessage(messageHelper.menuMessage(chatId));
			}
			// Matches
			else if (text.equalsIgnoreCase(Commands.MATCHES)) {
				Document doc = httpService.getDocument(HLTV + "/matches");
				botController.sendMessage(hltvService.matchesMessage(chatId, doc));
			}
			// Results
			else if (text.equalsIgnoreCase(Commands.RESULTS)) {
				Document doc = httpService.getDocument(HLTV + "/results");
				botController.sendMessage(hltvService.resultsMessage(chatId, doc));
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
				botController.sendMessage(twitchService.getStreams(chatId, false));
				botController.sendMessage(twitchService.nextPage(chatId));
			}
			// Favorite Teams
			else if (text.equalsIgnoreCase(Commands.TEAMS)) {
				botController.sendMessage(favoriteTeamsService.favoriteTeams(chatId));
			}

			else if (text.matches(TEAM_REGEXP + ".*")) {
				// Insert/Update (.команды+)
				if (text.matches(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP + COUNTRY_REGEXP)) {
					String team = text.replaceAll("\\[..\\]", "").replaceAll(TEAM_REGEXP + PLUS_REGEXP, "").trim();
					String countryCode = text.replaceAll(TEAM_REGEXP + PLUS_REGEXP + NAME_REGEXP, "")
							.replaceAll("\\[", "").replaceAll("\\]", "");
					String dbResult = favoriteTeamsService.updateOrSaveTeam(chatId, team, countryCode);
					botController.sendMessage(messageHelper.dbResult(chatId, dbResult, team));
				}
				// Delete (.команды-)
				else if (text.matches(TEAM_REGEXP + MINUS_REGEXP + NAME_REGEXP)) {
					String team = text.replaceAll(TEAM_REGEXP + MINUS_REGEXP, "").trim();
					String dbResult = favoriteTeamsService.deleteTeam(chatId, team);
					botController.sendMessage(messageHelper.dbResult(chatId, dbResult, team));
				} else {
					botController.sendMessage(messageHelper.teamsFormat(chatId));
				}
			}
			// ScoreBot
			else if (text.equals(Commands.SCOREBOT)) {
				botController.sendMessage(hltvService.scorebotHelpMessage(chatId));
			} else if (text.matches(START_REGEXP)) {
				hltvService.startBroadcast(chatId, text.substring(7));
			} else if (text.equalsIgnoreCase(Commands.STOP)) {
				hltvService.stopBroadcast(chatId);
				botController.sendMessage(messageHelper.stoped(chatId));
			}
			// Private and mentioned message
			else if (StringUtils.startsWith(update.getMessage().getText(), "@" + botController.getBotUsername())
					|| update.getMessage().getChat().isUserChat()) {
				botController.sendMessage(chatId, messageHelper.sticker(chatId, uniqCount));
				Document doc = httpService
						.getDocument("https://api.forismatic.com/api/1.0/?method=getQuote&format=html&lang=ru");
				botController.sendMessage(messageHelper.cite(chatId, doc));
			}
		}

		// Check call back from Menu
		if (update.getCallbackQuery() != null) {
			String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
			CallbackQuery callBack = update.getCallbackQuery();
			if (isTimeout(callBack)) {
				botController.sendMessage(messageHelper.oops(chatId));
				botController.deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
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
				botController.sendMessage(hltvService.matchesMessage(chatId, doc));
			}
			if (data.equals(CallBackData.RESULTS)) {
				Document doc = httpService.getDocument(HLTV + "/results");
				botController.sendMessage(hltvService.resultsMessage(chatId, doc));
			}
			if (data.equals(CallBackData.TEAMS)) {
				botController.sendMessage(favoriteTeamsService.favoriteTeams(chatId));
			}
			if (data.equals(CallBackData.STREAMS)) {
				botController.sendMessage(twitchService.getStreams(chatId, false));
				botController.sendMessage(twitchService.nextPage(chatId));
			}
			if (data.equals(CallBackData.NEXT_PAGE)) {
				botController.sendMessage(twitchService.getStreams(chatId, true));
				botController.sendMessage(twitchService.nextPage(chatId));
			}
			if (data.equals(CallBackData.SCOREBOT)) {
				botController.sendMessage(hltvService.scorebotHelpMessage(chatId));
			}

			botController.deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
		}
	}

	private void topPlayers(String chatId, int count) {
		String year = String.valueOf(LocalDate.now().getYear());
		Document doc = httpService
				.getDocument(HLTV + "/stats/players?startDate=" + year + "-01-01&endDate=" + year + "-12-31");
		botController.sendMessage(hltvService.topPlayersMessage(chatId, doc, count));
	}

	private void topTeams(String chatId, int count) {
		Document doc = httpService.getDocument(HLTV + "/ranking/teams");
		botController.sendMessage(hltvService.topTeamsMessage(chatId, doc, count));
	}

	private <T extends BotApiObject> boolean isTimeout(T obj) {
		if (obj instanceof Update && obj != null) {
			Update update = (Update) obj;
			long responseTime = Instant.now().getEpochSecond() - update.getMessage().getDate();
			if (responseTime > messageTimeout) {
				return true;
			}
		}
		if (obj instanceof CallbackQuery && obj != null) {
			CallbackQuery callBack = (CallbackQuery) obj;
			long responseTime = Instant.now().getEpochSecond() - callBack.getMessage().getDate();
			if (responseTime > callBackTimeout) {
				return true;
			}
		}
		return false;
	}

	public BotService setUpdate(Update update) {
		this.update = update;
		return this;
	}

}
