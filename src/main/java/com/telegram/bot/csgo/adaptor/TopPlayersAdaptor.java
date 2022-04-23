package com.telegram.bot.csgo.adaptor;

import java.time.LocalDate;
import static com.telegram.bot.csgo.model.message.EmojiCode.SPORT_MEDAL;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.HLINK;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.HREF;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.IMG;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.KD_DIFF_COL;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.PLAYER_COL;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.RATING_COL;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.STATS_DETAIL;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.STATS_TABLE_ROW;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.TEAM_COL;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.TH;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.TITLE;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.TR;
import static com.telegram.bot.csgo.helper.MessageHelper.BOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.COMMA;
import static com.telegram.bot.csgo.helper.MessageHelper.HASH;
import static com.telegram.bot.csgo.helper.MessageHelper.LINE_BRAKE;
import static com.telegram.bot.csgo.helper.MessageHelper.LINK_END;
import static com.telegram.bot.csgo.helper.MessageHelper.LINK_HLTV;
import static com.telegram.bot.csgo.helper.MessageHelper.UNBOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.UNLINK;
import static com.telegram.bot.csgo.helper.MessageHelper.WHITESPACE;
import com.telegram.bot.csgo.model.message.HtmlMessage;
import com.telegram.bot.csgo.service.EmojiService;
import com.telegram.bot.csgo.service.FlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopPlayersAdaptor {

    private final FlagService flagService;
    private final EmojiService emojiService;

    private static final String TOP_PLAYERS_STR = "CS:GO World Top Players";
    private static final String TEAM_STR = "Team";
    private static final String TEAMS_STR = TEAM_STR + 's';

    public SendMessage topPlayers(String chatId, Document doc, Integer count) {
        var message = prepareMessage(doc, count);
        log.debug("TopPlayers final message: {}", message);
        return new HtmlMessage(chatId, message);
    }

    public String prepareMessage(Document doc, Integer count) {
        var textMessage = new StringBuilder();
        var year = String.valueOf(LocalDate.now().getYear());
        textMessage.append(emojiService.getEmoji(SPORT_MEDAL))
                .append(BOLD)
                .append(TOP_PLAYERS_STR)
                .append(WHITESPACE)
                .append(year)
                .append(LINE_BRAKE);
        var stat = doc.select(STATS_TABLE_ROW).select(TH);
        for (var i = 0; i < stat.size(); i++) {
            if (i == stat.size() - 1) {
                textMessage.append(stat.get(i).text());
            } else {
                textMessage.append(stat.get(i).text().replace(TEAMS_STR, TEAM_STR))
                        .append(COMMA)
                        .append(WHITESPACE);
            }
        }
        textMessage.append(UNBOLD);
        var number = 1;
        for (var value : doc.select(TR)) {
            if (value.select(STATS_DETAIL).first() == null) {
                continue;
            }
            if (number > count) {
                break;
            }
            textMessage.append(BOLD)
                    .append(HASH)
                    .append(number)
                    .append(flagService
                            .flagUnicodeFromCountry(value.select(PLAYER_COL).select(IMG).attr(TITLE)))
                    .append(UNBOLD)
                    .append(WHITESPACE)
                    .append(LINK_HLTV)
                    .append(value.select(PLAYER_COL).select(HLINK).attr(HREF))
                    .append(LINK_END)
                    .append(value.select(PLAYER_COL).text())
                    .append(UNLINK)
                    .append(COMMA)
                    .append(WHITESPACE)
                    .append(value.select(TEAM_COL).select(IMG).attr(TITLE))
                    .append(COMMA)
                    .append(WHITESPACE)
                    .append(value.select(STATS_DETAIL).get(0).text())
                    .append(COMMA)
                    .append(WHITESPACE)
                    .append(value.select(KD_DIFF_COL).text())
                    .append(COMMA)
                    .append(WHITESPACE)
                    .append(value.select(STATS_DETAIL).get(1).text())
                    .append(COMMA)
                    .append(WHITESPACE)
                    .append(value.select(RATING_COL).text())
                    .append(LINE_BRAKE);
            number++;
        }
        return textMessage.toString();
    }

}
