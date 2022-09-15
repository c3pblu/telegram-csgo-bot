package com.telegram.bot.csgo.adaptor;

import java.util.stream.Collectors;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.CHANGE;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.HLINK;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.HREF;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MORE;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.MORE_LINK_QUERY;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.NAME;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.POINTS;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.POSITION;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.RANKED_TEAM;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.RANKING_NICKNAMES;
import static com.telegram.bot.csgo.helper.HtmlTagsHelper.REGIONAL_RANKING_HEADER;
import static com.telegram.bot.csgo.helper.MessageHelper.BOLD;
import static com.telegram.bot.csgo.helper.MessageHelper.HASH;
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
import static com.telegram.bot.csgo.model.message.EmojiCode.MIL_MEDAL;
import static java.lang.String.join;
import com.telegram.bot.csgo.model.message.HtmlMessage;
import com.telegram.bot.csgo.service.EmojiService;
import com.telegram.bot.csgo.service.FlagService;
import com.telegram.bot.csgo.service.TeamCountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopTeamsAdaptor {

    private final TeamCountryService teamCountryService;
    private final FlagService flagService;
    private final EmojiService emojiService;

    public SendMessage topTeams(String chatId, Document doc, Integer count) {
        var message = prepareMessage(doc, count);
        log.debug("TopTeams final message: {}", message);
        return new HtmlMessage(chatId, message);
    }

    public String prepareMessage(Document doc, Integer count) {
        var textMessage = new StringBuilder();
        textMessage.append(emojiService.getEmoji(MIL_MEDAL))
                .append(BOLD)
                .append(doc.select(REGIONAL_RANKING_HEADER).text())
                .append(UNBOLD)
                .append(LINE_BRAKE);
        for (var team : doc.select(RANKED_TEAM)) {
            if (team.select(POSITION).text().equals(HASH + (count + 1))) {
                break;
            }
            var teamProfileUrl = team.select(MORE).select(HLINK).attr(HREF);
            log.debug("Team profile URL: {}", teamProfileUrl);
            var country = teamCountryService.getCountry(teamProfileUrl);
            var teamFlag = flagService.flagUnicodeFromCountry(country);
            var row = new StringBuilder();
            row.append(BOLD)
                    .append(team.select(POSITION).text())
                    .append(UNBOLD)
                    .append(WHITESPACE)
                    .append(LEFT_BRACKET)
                    .append(team.select(CHANGE).text())
                    .append(RIGHT_BRACKET)
                    .append(WHITESPACE)
                    .append(teamFlag)
                    .append(LINK_HLTV)
                    .append(team.select(MORE).select(MORE_LINK_QUERY).attr(HREF))
                    .append(LINK_END)
                    .append(team.select(NAME).text())
                    .append(UNLINK)
                    .append(WHITESPACE)
                    .append(team.select(POINTS).text())
                    .append(WHITESPACE)
                    .append(LEFT_SQUARE_BRACKET);
            var playersNames = team.select(RANKING_NICKNAMES)
                    .stream()
                    .map(Element::text)
                    .collect(Collectors.toList());
            row.append(join(", ", playersNames))
                    .append(RIGHT_SQUARE_BRACKET)
                    .append(LINE_BRAKE);
            textMessage.append(row);
        }
        return textMessage.toString();
    }

}
