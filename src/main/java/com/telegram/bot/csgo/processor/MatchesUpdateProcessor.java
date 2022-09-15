package com.telegram.bot.csgo.processor;

import static com.telegram.bot.csgo.helper.CommandHelper.MATCHES_COMMAND;
import static com.telegram.bot.csgo.helper.MessageHelper.HLTV_URL;
import com.telegram.bot.csgo.adaptor.MatchesAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.http.HttpService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class MatchesUpdateProcessor extends UpdateProcessor {

    private final BotController botController;
    private final HttpService httpService;
    private final MatchesAdaptor matchesAdaptor;

    @Override
    public void process(@NonNull Update update) {
        if (isMatchesCommand(update)) {
            var doc = httpService.get(HLTV_URL + MATCHES_COMMAND, null, Jsoup::parse);
            botController.send(matchesAdaptor.matches(getChatId(update), doc));
            deleteMenu(botController, update);
        }
    }

    @SuppressWarnings("OverlyComplexBooleanExpression")
    private boolean isMatchesCommand(Update update) {
        return (update.hasMessage() && MATCHES_COMMAND.equalsIgnoreCase(update.getMessage().getText()))
                || (update.hasCallbackQuery() && MATCHES_COMMAND.equals(update.getCallbackQuery().getData()));
    }
}
