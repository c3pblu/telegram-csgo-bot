package com.telegram.bot.csgo.processor;

import static com.telegram.bot.csgo.helper.CommandHelper.RESULTS_COMMAND;
import static com.telegram.bot.csgo.helper.MessageHelper.HLTV_URL;
import com.telegram.bot.csgo.adaptor.ResultsAdaptor;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.service.http.HttpService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class ResultsUpdateProcessor extends UpdateProcessor {

    private final BotController botController;
    private final HttpService httpService;
    private final ResultsAdaptor resultsAdaptor;

    @Override
    public void process(@NonNull Update update) {
        if (isResultCommand(update)) {
            var doc = httpService.getAsDocument(HLTV_URL + RESULTS_COMMAND);
            botController.send(resultsAdaptor.results(getChatId(update), doc));
            deleteMenu(botController, update);
        }
    }

    @SuppressWarnings("OverlyComplexBooleanExpression")
    private boolean isResultCommand(Update update) {
        return (update.hasMessage() && RESULTS_COMMAND.equalsIgnoreCase(update.getMessage().getText()))
                || (update.hasCallbackQuery() && RESULTS_COMMAND.equals(update.getCallbackQuery().getData()));
    }
}
