package com.telegram.bot.csgo.adaptor;

import java.time.ZoneOffset;
import java.util.Locale;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.DATA_UNIX;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.FIRST_TEAM;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.HLINK;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.HREF;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.LIVE_MATCH;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.LIVE_MATCHES;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.LIVE_MATCHES_CONTAINER;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.LOST;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MAP_HOLDER;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MAP_NAME;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MATCH_DAY_HEADLINE;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MATCH_EVENT_NAME;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MATCH_INFO_EMPTY;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MATCH_META;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MATCH_TEAM_NAME;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MATCH_TIME;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.RESULTS_LEFT;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.RESULTS_TEAM_SCORE;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.SECOND_TEAM;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.STARS_FADED;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.UPCOMING_MATCH;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.UPCOMING_MATCHES_CONTAINER;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.UPCOMING_MATCHES_SECTION;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.WON;
import static com.telegram.bot.csgo.helper.MessageHelper.BOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.COLON;
import static com.telegram.bot.csgo.helper.MessageHelper.HLTV_URL;
import static com.telegram.bot.csgo.helper.MessageHelper.LEFT_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.LINE_BRAKE;
import static com.telegram.bot.csgo.helper.MessageHelper.LINK_END;
import static com.telegram.bot.csgo.helper.MessageHelper.LINK_HLTV;
import static com.telegram.bot.csgo.helper.MessageHelper.MINUS;
import static com.telegram.bot.csgo.helper.MessageHelper.RIGHT_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.UNBOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.UNLINK;
import static com.telegram.bot.csgo.helper.MessageHelper.WHITESPACE;
import static com.telegram.bot.csgo.model.message.EmojiCode.CUP;
import static com.telegram.bot.csgo.model.message.EmojiCode.EXCL_MARK;
import static com.telegram.bot.csgo.model.message.EmojiCode.SQUARE;
import static com.telegram.bot.csgo.model.message.EmojiCode.STAR;
import static com.telegram.bot.csgo.model.message.EmojiCode.VS;
import static java.lang.Long.parseLong;
import static java.time.LocalDateTime.ofEpochSecond;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.IntStream.rangeClosed;
import com.telegram.bot.csgo.model.message.HtmlMessage;
import com.telegram.bot.csgo.service.EmojiService;
import com.telegram.bot.csgo.service.FavoriteTeamService;
import com.telegram.bot.csgo.service.http.HttpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchesAdaptor {

    private static final String LIVE_MATCHES_STR = "Live matches";
    private static final String UPCOMING_MATCHES_STR = "Upcoming CS:GO matches";

    private final FavoriteTeamService favoriteTeamService;
    private final HttpService httpService;
    private final EmojiService emojiService;

    public SendMessage matches(String chatId, Document doc) {
        var message = prepareMessage(chatId, doc);
        log.debug("Matches final message: {}", message);
        return new HtmlMessage(chatId, message);
    }

    private String prepareMessage(String chatId, Document doc) {
        var textMessage = new StringBuilder();
        // Live Matches
        var matchesContainer = doc.select(LIVE_MATCHES_CONTAINER).select(LIVE_MATCHES);
        if (!matchesContainer.isEmpty()) {
            textMessage.append(BOLD)
                    .append(LIVE_MATCHES_STR)
                    .append(UNBOLD)
                    .append(emojiService.getEmoji(EXCL_MARK))
                    .append(LINE_BRAKE);
            for (var match : matchesContainer.select(LIVE_MATCH)) {
                textMessage.append(emojiService.getEmoji(CUP))
                        .append(LINK_HLTV)
                        .append(match.select(HLINK).attr(HREF))
                        .append(LINK_END)
                        .append(match.select(MATCH_EVENT_NAME).text())
                        .append(UNLINK)
                        .append(LINE_BRAKE)
                        .append(favoriteTeamService
                                .favoriteTeam(chatId, match.select(MATCH_TEAM_NAME).get(0).text(), true))
                        .append(WHITESPACE)
                        .append(emojiService.getEmoji(VS))
                        .append(WHITESPACE)
                        .append(favoriteTeamService.favoriteTeam(chatId, match.select(MATCH_TEAM_NAME).get(1).text(),
                                true))
                        .append(WHITESPACE)
                        .append(LEFT_BRACKET)
                        .append(match.select(MATCH_META).text())
                        .append(RIGHT_BRACKET)
                        .append(WHITESPACE)
                        .append(getStars(match))
                        .append(LINE_BRAKE);
                var matchPage = httpService.getAsDocument(HLTV_URL + match.select(HLINK).attr(HREF));
                for (var map : matchPage.select(MAP_HOLDER)) {
                    var team1Score = map.select(RESULTS_TEAM_SCORE).get(0).text();
                    var team2Score = map.select(RESULTS_TEAM_SCORE).get(1).text();
                    // Is first team won?
                    if (!map.select(RESULTS_LEFT).get(0).select(WON).isEmpty()) {
                        team1Score = BOLD + team1Score + UNBOLD;
                    }
                    // Is first team lost?
                    if (!map.select(RESULTS_LEFT).get(0).select(LOST).isEmpty()) {
                        team2Score = BOLD + team2Score + UNBOLD;
                    }
                    textMessage.append(map.select(MAP_NAME).text())
                            .append(COLON)
                            .append(WHITESPACE)
                            .append(team1Score)
                            .append(MINUS)
                            .append(team2Score)
                            .append(LINE_BRAKE);
                }
                textMessage.append(LINE_BRAKE);
            }
        }
        // Upcoming Matches for today
        var todayMatches = doc.select(UPCOMING_MATCHES_CONTAINER)
                .select(UPCOMING_MATCHES_SECTION)
                .first();
        textMessage.append(BOLD)
                .append(UPCOMING_MATCHES_STR)
                .append(LINE_BRAKE);
        textMessage.append(todayMatches.select(MATCH_DAY_HEADLINE).text())
                .append(UNBOLD)
                .append(LINE_BRAKE);
        for (var match : todayMatches.select(UPCOMING_MATCH)) {
            var unixTime = parseLong(match.select(MATCH_TIME).attr(DATA_UNIX));
            var localTime = ofEpochSecond((unixTime / 1000) + 10800, 0, ZoneOffset.UTC);
            var formatter = ofPattern("HH:mm", Locale.ENGLISH);
            var formattedTime = localTime.format(formatter);
            textMessage.append(BOLD)
                    .append(formattedTime)
                    .append(UNBOLD)
                    .append(WHITESPACE)
                    .append(MINUS)
                    .append(WHITESPACE);
            if (match.select(MATCH_INFO_EMPTY).isEmpty()) {
                textMessage.append(favoriteTeamService.favoriteTeam(chatId, match.select(FIRST_TEAM).text(), true))
                        .append(WHITESPACE)
                        .append(emojiService.getEmoji(VS))
                        .append(WHITESPACE)
                        .append(favoriteTeamService.favoriteTeam(chatId, match.select(SECOND_TEAM).text(), true))
                        .append(WHITESPACE)
                        .append(LEFT_BRACKET)
                        .append(match.select(MATCH_META).text())
                        .append(RIGHT_BRACKET)
                        .append(WHITESPACE);
            } else {
                textMessage.append(match.select(MATCH_INFO_EMPTY).text())
                        .append(WHITESPACE);
            }
            textMessage.append(getStars(match))
                    .append(emojiService.getEmoji(SQUARE))
                    .append(WHITESPACE)
                    .append(LINK_HLTV)
                    .append(match.select(HLINK).attr(HREF))
                    .append(LINK_END)
                    .append(match.select(MATCH_EVENT_NAME).text())
                    .append(UNLINK)
                    .append(LINE_BRAKE);
        }
        return textMessage.toString();
    }

    private StringBuilder getStars(Element match) {
        var stars = new StringBuilder();
        rangeClosed(0, 4 - match.select(STARS_FADED).size())
                .forEach(i -> stars.append(emojiService.getEmoji(STAR)));
        return stars;
    }
}
