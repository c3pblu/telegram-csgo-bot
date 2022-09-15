package com.telegram.bot.csgo.processor;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.telegram.bot.csgo.helper.CommandHelper.QUOTE_COMMAND;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.CITE;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.SMALL;
import static com.telegram.bot.csgo.helper.MessageHelper.BOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.LINE_BRAKE;
import static com.telegram.bot.csgo.helper.MessageHelper.UNBOLD;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.telegram.telegrambots.meta.api.methods.send.SendSticker.builder;
import com.telegram.bot.csgo.config.properties.BotProperties;
import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.domain.Sticker;
import com.telegram.bot.csgo.model.message.HtmlMessage;
import com.telegram.bot.csgo.service.http.HttpService;
import com.telegram.bot.csgo.service.StickerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class QuoteUpdateProcessor extends UpdateProcessor {

    private static final String QUOTE_URL = "https://api.forismatic.com/api/1.0/?method=getQuote&format=html&lang=ru";
    private static final Character MENTION = '@';

    private final BotController botController;
    private final HttpService httpService;
    private final StickerService stickerService;
    private final BotProperties botProperties;

    private final Map<String, List<Sticker>> chatLastStickers = new ConcurrentHashMap<>();

    @Override
    public void process(@NonNull Update update) {
        if (isMessageToBot(update)) {
            var chatId = getChatId(update);
            var sticker = getSticker(chatId, botProperties.getMessage().getUniqCount());
            botController.sendSticker(sticker);
            var doc = httpService.getAsDocument(QUOTE_URL);
            botController.send(createQuote(chatId, doc));
        }
    }

    private boolean isMessageToBot(Update update) {
        return update.hasMessage()
                && (startsWith(update.getMessage().getText(), MENTION + botProperties.getName())
                || QUOTE_COMMAND.equalsIgnoreCase(update.getMessage().getText()));
    }

    private SendSticker getSticker(String chatId, Integer uniqCount) {
        var stickers = stickerService.getAllStickers();
        var randomSize = stickers.size();
        var randomValue = new SecureRandom().nextInt(randomSize);
        var selectedSticker = stickers.get(randomValue);
        var lastStickers = chatLastStickers.containsKey(chatId)
                ? chatLastStickers.get(chatId)
                : new ArrayList<Sticker>();
        while (lastStickers.contains(selectedSticker)) {
            randomValue = new SecureRandom().nextInt(randomSize);
            selectedSticker = stickers.get(randomValue);
        }
        if (lastStickers.size() >= uniqCount) {
            lastStickers.remove(0);
        }
        lastStickers.add(selectedSticker);
        chatLastStickers.put(chatId, lastStickers);
        return builder().sticker(new InputFile(selectedSticker.getCode()))
                .chatId(chatId)
                .build();
    }

    private static SendMessage createQuote(String chatId, Document doc) {
        var text = new StringBuilder(doc.select(CITE).text());
        var author = doc.select(SMALL).text();
        if (isNotEmpty(author)) {
            text.append(LINE_BRAKE)
                    .append(BOLD)
                    .append(author)
                    .append(UNBOLD);
        }
        return new HtmlMessage(chatId, text.toString());
    }
}
