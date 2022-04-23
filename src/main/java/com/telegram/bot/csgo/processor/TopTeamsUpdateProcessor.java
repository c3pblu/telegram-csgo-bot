package com.telegram.bot.csgo.processor;

import java.util.regex.Pattern;
import static com.telegram.bot.csgo.helper.MessageHelper.HLTV_URL;
import static java.lang.Integer.parseInt;
import com.telegram.bot.csgo.adaptor.TopTeamsAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.http.HttpService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TopTeamsUpdateProcessor implements UpdateProcessor {

    private final BotController botController;
    private final HttpService httpService;
    private final TopTeamsAdaptor topTeamsAdaptor;

    private static final String TEAMS_PATH = "/ranking/teams";
    private static final Pattern TOP_TEAMS_PATTERN = Pattern.compile("(/top)([123]0)");

    @Override
    public void process(@NonNull Update update) {
        if (update.hasMessage()) {
            var text = update.getMessage().getText();
            if (isTopTeamsCommand(text)) {
                var message = prepareMessage(text, update);
                botController.send(message);
            }
        }
        if ((update.hasCallbackQuery())) {
            var data = update.getCallbackQuery().getData();
            if (isTopTeamsCallback(data)) {
                var message = prepareMessage(data, update);
                botController.send(message);
                deleteMenu(botController, update);
            }
        }
    }

    private SendMessage prepareMessage(String text, Update update) {
        var matcher = TOP_TEAMS_PATTERN.matcher(text);
        if (matcher.find()) {
            var count = parseInt(matcher.group(2));
            return topTeams(getChatId(update), count);
        }
        return null;
    }

    private boolean isTopTeamsCommand(String text) {
        return TOP_TEAMS_PATTERN.matcher(text).matches();
    }

    private boolean isTopTeamsCallback(String data) {
        return data != null && TOP_TEAMS_PATTERN.matcher(data).matches();
    }

    private SendMessage topTeams(String chatId, int count) {
        var doc = httpService.getAsDocument(HLTV_URL + TEAMS_PATH);
        return topTeamsAdaptor.topTeams(chatId, doc, count);
    }
}
