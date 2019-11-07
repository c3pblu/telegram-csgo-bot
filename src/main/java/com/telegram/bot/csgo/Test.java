package com.telegram.bot.csgo;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Test {
    public static void main(String args[]) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod("https://www.hltv.org/results");
        get.setFollowRedirects(true);
        get.setRequestHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        client.executeMethod(get);
        String response = get.getResponseBodyAsString();
        get.releaseConnection();
        Document doc = Jsoup.parse(response);
        StringBuilder textMessage = new StringBuilder();

        Elements subLists = doc.select("div.results-sublist");
        int i = 0;
        for (Element resultList : subLists) {
        	if (i > 1) break;
        	String headerText = resultList.select("span.standard-headline").text();
        	if (headerText.isEmpty()) {
        		textMessage.append("<b>Featured Results</b>");
        	}
        	
        	textMessage.append("<b>")
        	.append(headerText)
        	.append("</b>\n");
  
        	for (Element resultCon : resultList.select("div.result-con")) {
        		Element team1 = resultCon.select("div.team").get(0);
        		Element team2 = resultCon.select("div.team").get(1);
        		String team1String = resultCon.select("div.team").get(0).text();
        		String team2String = resultCon.select("div.team").get(1).text();
        		
        		if (team1.hasClass("team-won")) {
        			textMessage.append("<b>")
        			.append(team1String)
        			.append("</b>");
        		} else {
        			textMessage.append(team1String);
        		}
        		
        		textMessage.append(" ")
        		.append(resultCon.select("td.result-score").text())
        		.append(" ");
        		
        		if (team2.hasClass("team-won")) {
        			textMessage.append("<b>")
        			.append(team2String)
        			.append("</b>");
        		} else {
        			textMessage.append(team2String);
        		}
        		
        		textMessage.append(" (")
        		.append(resultCon.select("div.map-text").text())
        		.append(") \uD83C\uDFC6 ")
        		.append(resultCon.select("td.event").text());
        		
        		textMessage.append("\n");

        	}
        	textMessage.append("\n");
        	i++;
        }

        System.out.println(textMessage);
    }
}
