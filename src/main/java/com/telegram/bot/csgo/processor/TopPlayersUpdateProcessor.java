package com.telegram.bot.csgo.processor;

import java.util.regex.Pattern;
import static com.telegram.bot.csgo.helper.MessageHelper.HLTV_URL;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;
import static java.time.LocalDate.now;
import com.telegram.bot.csgo.adaptor.TopPlayersAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.http.HttpService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TopPlayersUpdateProcessor extends UpdateProcessor {

    private static final String STATS_PATH_PREFIX = "/stats/players?startDate=";
    private static final String END_DATE_POSTFIX = "-01-01&endDate=";
    private static final String LAST_DAY_POSTFIX = "-12-31";
    private static final Pattern TOP_PLAYERS_PATTERN = Pattern.compile("(/top)([123]0)players");

    private final BotController botController;
    private final HttpService httpService;
    private final TopPlayersAdaptor topPlayersAdaptor;

    @Override
    public void process(@NonNull Update update) {
        if (update.hasMessage()) {
            var text = update.getMessage().getText();
            if (isTopPlayersCommand(text)) {
                var message = prepareMessage(text, update);
                botController.send(message);
            }
        }
        if (update.hasCallbackQuery()) {
            var data = update.getCallbackQuery().getData();
            if (isTopPlayersCallback(data)) {
                var message = prepareMessage(data, update);
                botController.send(message);
                deleteMenu(botController, update);
            }
        }
    }

    private SendMessage prepareMessage(String text, Update update) {
        var matcher = TOP_PLAYERS_PATTERN.matcher(text);
        if (matcher.find()) {
            var count = parseInt(matcher.group(2));
            return topPlayers(getChatId(update), count);
        }
        return null;
    }

    private SendMessage topPlayers(String chatId, int count) {
        var year = valueOf(now().getYear());
        var url = HLTV_URL + STATS_PATH_PREFIX + year + END_DATE_POSTFIX + year + LAST_DAY_POSTFIX;
        var doc = httpService.getAsDocument(url);
        return topPlayersAdaptor.topPlayers(chatId, doc, count);
    }

    private static boolean isTopPlayersCommand(String text) {
        return TOP_PLAYERS_PATTERN.matcher(text).matches();
    }

    private static boolean isTopPlayersCallback(String data) {
        return data != null && TOP_PLAYERS_PATTERN.matcher(data).matches();
    }

}
