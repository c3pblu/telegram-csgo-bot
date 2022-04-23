package com.telegram.bot.csgo.adaptor;

import static com.telegram.bot.csgo.model.message.EmojiCode.CUP;
import static com.telegram.bot.csgo.model.message.EmojiCode.SQUARE;
import static com.telegram.bot.csgo.model.message.EmojiCode.STAR;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.EVENT;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.HLINK;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.HREF;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.I;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MAP_TEXT;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.RESULTS_SUBLIST;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.RESULT_CON;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.RESULT_SCORE;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.STANDARD_HEADLINE;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.STARS;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.TAB;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.TAB_HOLDER;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.TEAM;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.TEAM_WON;
import static com.telegram.bot.csgo.helper.MessageHelper.BOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.LEFT_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.LEFT_SQUARE_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.LINE_BRAKE;
import static com.telegram.bot.csgo.helper.MessageHelper.LINK_END;
import static com.telegram.bot.csgo.helper.MessageHelper.LINK_HLTV;
import static com.telegram.bot.csgo.helper.MessageHelper.RIGHT_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.RIGHT_SQUARE_BRACKET;
import static com.telegram.bot.csgo.helper.MessageHelper.UNBOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.UNLINK;
import static com.telegram.bot.csgo.helper.MessageHelper.WHITESPACE;
import com.telegram.bot.csgo.model.message.HtmlMessage;
import com.telegram.bot.csgo.service.EmojiService;
import com.telegram.bot.csgo.service.FavoriteTeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResultsAdaptor {

    private final FavoriteTeamService favoriteTeamService;
    private final EmojiService emojiService;

    private static final String RESULTS_STR = "Results";

    public SendMessage results(String chatId, Document doc) {
        var message = prepareMessage(chatId, doc);
        log.debug("Results final message: {}", message);
        return new HtmlMessage(chatId, message);
    }

    private String prepareMessage(String chatId, Document doc) {
        var textMessage = new StringBuilder();
        var featuredNum = 0;
        for (var resultList : doc.select(RESULTS_SUBLIST)) {
            var headerText = resultList.select(STANDARD_HEADLINE).text();
            if (headerText.isEmpty()) {
                headerText = doc.select(TAB_HOLDER).select(TAB).get(featuredNum++).text();
            }
            textMessage.append(emojiService.getEmoji(CUP))
                    .append(WHITESPACE)
                    .append(BOLD)
                    .append(headerText)
                    .append(UNBOLD)
                    .append(LINE_BRAKE);
            for (var resultCon : resultList.select(RESULT_CON)) {
                var team1String = favoriteTeamService.favoriteTeam(chatId, resultCon.select(TEAM).get(0).text(),
                        false);
                var team2String = favoriteTeamService.favoriteTeam(chatId, resultCon.select(TEAM).get(1).text(),
                        false);
                if (resultCon.select(TEAM).get(0).hasClass(TEAM_WON)) {
                    textMessage.append(BOLD)
                            .append(team1String)
                            .append(UNBOLD);
                } else {
                    textMessage.append(team1String);
                }
                textMessage
                        .append(WHITESPACE)
                        .append(LEFT_SQUARE_BRACKET)
                        .append(resultCon.select(RESULT_SCORE).text())
                        .append(RIGHT_SQUARE_BRACKET)
                        .append(WHITESPACE);
                if (resultCon.select(TEAM).get(1).hasClass(TEAM_WON)) {
                    textMessage.append(BOLD)
                            .append(team2String)
                            .append(UNBOLD);
                } else {
                    textMessage.append(team2String);
                }
                textMessage.append(WHITESPACE)
                        .append(LEFT_BRACKET)
                        .append(resultCon.select(MAP_TEXT).text())
                        .append(RIGHT_BRACKET)
                        .append(WHITESPACE)
                        .append(getStars(resultCon))
                        .append(emojiService.getEmoji(SQUARE))
                        .append(WHITESPACE)
                        .append(LINK_HLTV).append(resultCon.select(HLINK).attr(HREF))
                        .append(LINK_END)
                        .append(resultCon.select(EVENT).text())
                        .append(UNLINK)
                        .append(WHITESPACE)
                        .append(LINE_BRAKE);
            }
            textMessage.append(LINE_BRAKE);
            if (headerText.startsWith(RESULTS_STR)) {
                break;
            }
        }
        return textMessage.toString();
    }

    private StringBuilder getStars(Element match) {
        var stars = new StringBuilder();
        match.select(STARS).select(I)
                .forEach(star -> stars.append(emojiService.getEmoji(STAR)));
        return stars;
    }
}
