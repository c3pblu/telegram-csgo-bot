package com.telegram.bot.csgo.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static com.telegram.bot.csgo.helper.CommandHelper.STREAMS_NEXT_PAGE_CALLBACK;
import static com.telegram.bot.csgo.helper.MessageHelper.BOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.EMPTY_STRING;
import static com.telegram.bot.csgo.helper.MessageHelper.LEFT_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.LINE_BRAKE;
import static com.telegram.bot.csgo.helper.MessageHelper.LINK_END;
import static com.telegram.bot.csgo.helper.MessageHelper.LINK_HLTV;
import static com.telegram.bot.csgo.helper.MessageHelper.RIGHT_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.UNBOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.UNLINK;
import static com.telegram.bot.csgo.helper.MessageHelper.WHITESPACE;
import static com.telegram.bot.csgo.model.message.EmojiCode.EXCL_MARK;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.bot.csgo.config.properties.TwitchProperties;
import com.telegram.bot.csgo.model.message.HtmlMessage;
import com.telegram.bot.csgo.model.twitch.Stream;
import com.telegram.bot.csgo.model.twitch.Token;
import com.telegram.bot.csgo.model.twitch.TwitchStreams;
import com.telegram.bot.csgo.service.http.HttpService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwitchService {

    private final FlagService flagService;
    private final HttpService httpService;
    private final TwitchProperties twitchProperties;
    private final EmojiService emojiService;
    private final Map<String, String> chatPage = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(SNAKE_CASE);
    private String accessToken;

    private static final String OAUTH_PREFIX = "OAuth ";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CLIENT_ID_HEADER = "Client-ID";
    private static final String TOKEN_VALIDATE_URL = "https://id.twitch.tv/oauth2/validate";
    private static final String STREAMS_URL = "https://api.twitch.tv/helix/streams?game_id=32399&language=en&language=ru";
    private static final String TOKE_URL_PREFIX = "https://id.twitch.tv/oauth2/token?client_id=";
    private static final String CLIENT_SECRET_POSTFIX = "&client_secret=";
    private static final String GRANT_TYPE_POSTFIX = "&grant_type=client_credentials";
    private static final String AFTER_POSTFIX = "&after=";
    private static final String LIVE_STR = "Live";
    private static final String STREAMS_STR = "Streams on Twitch:";
    private static final String NEXT_PAGE_TITLE = "Next 20 Streams »";
    private static final String NEXT_PAGE_QUESTION = "Go to next Page?";

    public SendMessage getStreams(String chatId, boolean isNextPage) {
        if (accessToken == null || accessToken.isEmpty()) {
            updateToken();
        } else {
            validateAndUpdateToken();
        }
        var streams = getStreams(isNextPage, chatId);
        var nextPage = streams.getPagination().getCursor();
        log.debug("NextPage ID: {}", nextPage);
        chatPage.put(chatId, nextPage);
        return prepareMessage(chatId, streams.getData());
    }

    public SendMessage nextPage(String chatId) {
        var markUpInLine = InlineKeyboardMarkup.builder().keyboard(List.of(
                        List.of(InlineKeyboardButton.builder()
                                .text(NEXT_PAGE_TITLE)
                                .callbackData(STREAMS_NEXT_PAGE_CALLBACK)
                                .build())))
                .build();
        return SendMessage.builder()
                .replyMarkup(markUpInLine)
                .text(NEXT_PAGE_QUESTION)
                .chatId(chatId)
                .build();
    }

    private void validateAndUpdateToken() {
        var headers = new HttpHeaders();
        headers.add(AUTHORIZATION, OAUTH_PREFIX + accessToken);
        var validateTokenResult = httpService.get(TOKEN_VALIDATE_URL, headers, JSONObject::new);
        if (validateTokenResult == null) {
            updateToken();
        }
    }

    @SneakyThrows
    private void updateToken() {
        var newAccessTokenStr = httpService.post(TOKE_URL_PREFIX + twitchProperties.getClientId()
                + CLIENT_SECRET_POSTFIX + twitchProperties.getClientSecret() + GRANT_TYPE_POSTFIX, "", null, String.class);
        var newAccessToken = objectMapper.readValue(newAccessTokenStr, Token.class);
        if (newAccessToken != null) {
            this.accessToken = newAccessToken.getAccessToken();
        }
    }

    @SneakyThrows
    private TwitchStreams getStreams(boolean isNextPage, String chatId) {
        var url = STREAMS_URL;
        if (isNextPage) {
            var currentPage = chatPage.get(chatId);
            url = url + AFTER_POSTFIX + currentPage;
            log.debug("Current page ID: {}", currentPage);
        }
        var headers = new HttpHeaders();
        headers.add(CLIENT_ID_HEADER, twitchProperties.getClientId());
        headers.add(AUTHORIZATION, BEARER_PREFIX + accessToken);
        var streamsStr = httpService.get(url, headers, String.class);
        return objectMapper.readValue(streamsStr, TwitchStreams.class);
    }

    private SendMessage prepareMessage(String chatId, List<Stream> streams) {
        var textMessage = new StringBuilder();
        textMessage.append(BOLD)
                .append(LIVE_STR)
                .append(UNBOLD)
                .append(emojiService.getEmoji(EXCL_MARK))
                .append(BOLD)
                .append(STREAMS_STR)
                .append(UNBOLD)
                .append(LINE_BRAKE);
        streams.forEach(s ->
                textMessage.append(BOLD)
                        .append(LEFT_BRACKET)
                        .append(s.getViewerCount())
                        .append(RIGHT_BRACKET)
                        .append(UNBOLD)
                        .append(WHITESPACE)
                        .append(LINK_HLTV)
                        .append(s.getUserName())
                        .append(LINK_END)
                        .append(s.getUserName())
                        .append(UNLINK)
                        .append(WHITESPACE)
                        .append(flagService.flagUnicodeFromCountry(s.getLanguage().toUpperCase()))
                        .append(WHITESPACE)
                        .append(s.getTitle().replace("<", EMPTY_STRING).replace(">", EMPTY_STRING))
                        .append(LINE_BRAKE));
        log.debug("Streams final message: {}", textMessage);
        return new HtmlMessage(chatId, textMessage);
    }

}
