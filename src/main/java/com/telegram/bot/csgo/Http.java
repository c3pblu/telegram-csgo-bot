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

public class Http {

    public static SendMessage top30() throws IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod("https://www.hltv.org/ranking/teams");
        get.setFollowRedirects(true);
        get.setRequestHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        client.executeMethod(get);
        String response = get.getResponseBodyAsString();
        get.releaseConnection();

        Document doc = Jsoup.parse(response);
        Elements header = doc.select("div.regional-ranking-header");
        Elements rankedTeams = doc.select("div.ranked-team");


        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("markdown");

        StringBuilder textMessage = new StringBuilder();
        textMessage.append("*" + header.text() + "* \n");


        for (Element team : rankedTeams) {
            StringBuilder row = new StringBuilder();
            row.append(team.select("span.position").text() + " ");
            row.append(team.select("span.name").text() + " ");
            row.append(team.select("span.points").text() + " [");
            ArrayList<String> listPlayers = new ArrayList<>();
            for (Element player : team.select("div.rankingNicknames")) {
                listPlayers.add(player.text());
            }
            row.append(String.join(", ", listPlayers));
            row.append("] ");
            row.append(team.select("div.change").text() + "\n");

            textMessage.append(row);
        }

        System.out.println(textMessage.toString());
        sendMessage.setText(textMessage.toString());

        return sendMessage;
    }



}
