package com.telegram.bot.csgo.processor.impl;

import com.telegram.bot.csgo.controller.BotController;
import com.telegram.bot.csgo.model.HtmlMessage;
import com.telegram.bot.csgo.model.entity.Sticker;
import com.telegram.bot.csgo.processor.UpdateProcessor;
import com.telegram.bot.csgo.repository.StickerRepository;
import com.telegram.bot.csgo.service.HttpService;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QuoteUpdateProcessor implements UpdateProcessor {

    private final BotController botController;
    private final HttpService httpService;
    private final StickerRepository stickerRepository;

    @Value("${bot.message.uniq.count}")
    private Integer uniqCount;

    private final Map<String, List<Sticker>> chatLastStickers = new ConcurrentHashMap<>();

    @Autowired
    public QuoteUpdateProcessor(BotController botController, HttpService httpService, StickerRepository stickerRepository) {
        this.botController = botController;
        this.httpService = httpService;
        this.stickerRepository = stickerRepository;
    }

    private static final String QUOTE_COMMAND = ".цитата";

    @Override
    public void process(Update update) {
        if (update.hasMessage()
                && (StringUtils.startsWith(update.getMessage().getText(), "@" + botController.getBotUsername())
                        || QUOTE_COMMAND.equalsIgnoreCase(update.getMessage().getText()))) {
            String chatId = getChatId(update);
            botController.send(getSticker(chatId, uniqCount));
            Document doc = httpService
                    .getDocument("https://api.forismatic.com/api/1.0/?method=getQuote&format=html&lang=ru");
            botController.send(quote(chatId, doc));
        }
    }

    private SendMessage quote(String chatId, Document doc) {
        StringBuilder text = new StringBuilder();
        text.append(doc.select("cite").text());
        String author = doc.select("small").text();
        if (StringUtils.isNotEmpty(author)) {
            text.append("\n<b>").append(author).append("</b>");
        }
        return new HtmlMessage(chatId, String.valueOf(text));
    }

    private SendSticker getSticker(String chatId, Integer uniqCount) {
        List<Sticker> stickers = stickerRepository.findAll();
        int randomSize = stickers.size();
        int randomValue = new Random().nextInt(randomSize);
        Sticker selectedSticker = stickers.get(randomValue);
        List<Sticker> lastStickers = chatLastStickers.containsKey(chatId) ? chatLastStickers.get(chatId)
                : new ArrayList<>();
        while (lastStickers.contains(selectedSticker)) {
            randomValue = new Random().nextInt(randomSize);
            selectedSticker = stickers.get(randomValue);
        }
        if (lastStickers.size() >= uniqCount) {
            lastStickers.remove(0);
        }
        lastStickers.add(selectedSticker);
        chatLastStickers.put(chatId, lastStickers);
        return SendSticker.builder().sticker(new InputFile(selectedSticker.getSticker())).chatId(chatId).build();
    }

}
