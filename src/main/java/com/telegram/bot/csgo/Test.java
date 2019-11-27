package com.telegram.bot.csgo;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.telegram.bot.csgo.messages.Emoji;

public class Test {
    public static void main(String args[]) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod("https://www.hltv.org/ranking/teams");
        get.setFollowRedirects(true);
        get.setRequestHeader(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        get.setRequestHeader("Client-ID", "03fcufq34ixb7h0cb9ap9mr6i9e9y9");
        client.executeMethod(get);
        String response = get.getResponseBodyAsString();
        get.releaseConnection();
        
        StringBuilder textMessage = new StringBuilder();

        Document doc =Jsoup.parse(response);
        Elements header = doc.select("div.regional-ranking-header");
        Elements rankedTeams = doc.select("div.ranked-team");
        
        textMessage.append(Emoji.MIL_MEDAL.getCode()).append("<b>").append(header.text()).append("</b>\n");
        for (Element team : rankedTeams) {
            if (team.select("span.position").text().equals("#" + (10 + 1))) {
                break;
            }
            
            GetMethod get2 = new GetMethod("https://www.hltv.org" + team.select("div.more").select("a").attr("href"));
            get2.setFollowRedirects(true);
            get2.setRequestHeader(HttpHeaders.USER_AGENT,
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
            get2.setRequestHeader("Client-ID", "03fcufq34ixb7h0cb9ap9mr6i9e9y9");
            client.executeMethod(get2);
            String response2 = get2.getResponseBodyAsString();
            get2.releaseConnection();
            
            Document teamProfile = Jsoup.parse(response2);
            String teamCountry = teamProfile.select("div.team-country").text().trim();
            
            
            StringBuilder row = new StringBuilder();
            row.append("<b>").append(team.select("span.position").text()).append("</b> ")
            .append(teamCountry)
            .append(" (")
                    .append(team.select("div.change").text()).append(") ").append("<a href=\'https://hltv.org")
                    .append(team.select("div.more").select("a[class=details moreLink]").attr("href")).append("\'>")
                    .append(team.select("span.name").text()).append("</a> ").append(team.select("span.points").text())
                    .append(" [");
            ArrayList<String> listPlayers = new ArrayList<>();
            for (Element player : team.select("div.rankingNicknames")) {
                listPlayers.add(player.text());
            }
            row.append(String.join(", ", listPlayers)).append("]\n");
            textMessage.append(row);
        }

        System.out.println(textMessage);

    }
    
}
