package com.telegram.bot.csgo;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.IOException;
import java.util.ArrayList;

public class MessageHelper {

    public static SendMessage topTeams(int count) {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod("https://www.hltv.org/ranking/teams");
        get.setFollowRedirects(true);
        get.setRequestHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("html");
        try {
            client.executeMethod(get);
            String response = get.getResponseBodyAsString();
            get.releaseConnection();
            Document doc = Jsoup.parse(response);
            Elements header = doc.select("div.regional-ranking-header");
            Elements rankedTeams = doc.select("div.ranked-team");
            StringBuilder textMessage = new StringBuilder();
            textMessage.append("<b>" + header.text() + "</b> \n\n");
            for (Element team : rankedTeams) {
                if (count == 10 && team.select("span.position").text().equals("#11")) {
                    break;
                }
                StringBuilder row = new StringBuilder();
                row.append("<b>" + team.select("span.position").text() + "</b> (");
                row.append(team.select("div.change").text() + ") ");

                row.append("<b>" + team.select("span.name").text() + "</b> ");
                row.append(team.select("span.points").text() + " [");
                ArrayList<String> listPlayers = new ArrayList<>();
                for (Element player : team.select("div.rankingNicknames")) {
                    listPlayers.add(player.text());
                }
                row.append(String.join(", ", listPlayers));
                row.append("]\n");
                textMessage.append(row);
            }
            System.out.println(textMessage.toString());
            sendMessage.setText(textMessage.toString());
            return sendMessage;

        }  catch (IOException e) {
            e.printStackTrace();
            sendMessage.setText("Не смог получить данные с сайта...");
        }
        return sendMessage;
    }

    public SendMessage topPlayers() {
        return null;
    }


    public static SendMessage help() {
        SendMessage help = new SendMessage();
        help.setParseMode("markdown");
        help.setText("\u2139 Могу посмотреть, что там нового на HLTV.org\n" +
                "\n" +
                "Спрашивай, не стесняйся:\n" +
                "\u2705 *.хелп* - эта информация\n" +
                "\u2705 *.топ10* - Top 10 Команд\n" +
                "\u2705 *.топ30* - Top 30 Команд\n" +
                "\u2705 *.топ100игроков* - Top 100 Игроков");
        return help;
    }

    public static SendMessage toBot(String who) {
        SendMessage message = new SendMessage();
        message.setText("Ну все... Молись @" + who + " сейчас отхватишь! \uD83D\uDCAA");
        return message;
    }

}
