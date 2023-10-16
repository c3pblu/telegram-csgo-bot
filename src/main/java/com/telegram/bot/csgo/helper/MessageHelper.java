package com.telegram.bot.csgo.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageHelper {

    public static final Character LINE_BRAKE = '\n';
    public static final Character WHITESPACE = ' ';
    public static final Character COMMA = ',';
    public static final Character DOT = '.';
    public static final Character LEFT_SQUARE_BRACKET = '[';
    public static final Character RIGHT_SQUARE_BRACKET = ']';
    public static final Character LEFT_BRACKET = '(';
    public static final Character RIGHT_BRACKET = ')';
    public static final Character MINUS = '-';
    public static final Character COLON = ':';
    public static final String EMPTY_STRING = "";
    public static final String HASH = "#";
    public static final String BOLD = "<b>";
    public static final String UNBOLD = "</b>";
    public static final String DOUBLE_LINE_BRAKE = "\n\n";
    public static final String HLTV_URL = "https://hltv.org";
    public static final String LINK_HLTV = "<a href='" + HLTV_URL;
    public static final String LINK_END = "'>";
    public static final String UNLINK = "</a>";

    public static final String TEAMS_HELP_MESSAGE =
            """
                    Add team/Change team's country:
                    <b>/teams+Natus Vincere[UA]</b>
                    Delete team:
                    <b>/teams-Natus Vincere</b>
                    """;
    public static final String TEAMS_YOUR_FAVORITE_MESSAGE =
            """
                     Your favorite teams:
                                
                    """;
    public static final String TEAMS_EMPTY_FAVORITE_MESSAGE = "<b>You don't have favorite teams yet!</b> ";
}
