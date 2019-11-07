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
        GetMethod get = new GetMethod("https://www.hltv.org/matches");
        get.setFollowRedirects(true);
        get.setRequestHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        client.executeMethod(get);
        String response = get.getResponseBodyAsString();
        get.releaseConnection();
        Document doc = Jsoup.parse(response);
        StringBuilder textMessage = new StringBuilder();

        
        if (doc.select("div.live-match").size() > 1) {
            textMessage.append("Live matches!\n\n");
        }
        
        for (Element match : doc.select("div.live-match")) {
        	if (match.text().isEmpty()) {
        		continue;
        	}
        	
			textMessage.append(match.select("div.event-name").text()).append("\n")
					.append(match.select("span.team-name").get(0).text()).append(" vs ")
					.append(match.select("span.team-name").get(1).text()).append(" (")
					.append(match.select("tr.header").select("td.bestof").text()).append(")\n");
        	
        	Elements maps = match.select("tr.header").select("td.map");
        	int numMaps = maps.size();
        	
        	for (int i = 0; i < numMaps; i++) {
        		StringBuilder sb = new StringBuilder();
        		
        		String first = match.select("td.livescore").select("span[data-livescore-map=" + (i + 1) + "]").get(0).text();
        		String second = match.select("td.livescore").select("span[data-livescore-map=" + (i + 1) + "]").get(1).text();
        		
        		
        		if (!first.equals("16") && !first.equals("0") && !first.equals("-") && !second.equals("16") && !second.equals("0") && !second.equals("-")) {
        			second = second.concat(" Live!");
        		}
        		else if (second.equals("16")) {
        			second = "<b>16</b>";
        		}
        		
        		else if (first.equals("16")) {
        			first = "<b>16</b>";
        		}
        		
        		sb.append(maps.get(i).text())
        		.append(": ")
        		.append(first)
        		.append("-")
        		.append(second)
        		.append("\n");
        		textMessage.append(sb);

        	}
        	
        	textMessage.append("\n");

        }
        
        textMessage.append("<b>Upcoming CS:GO matches</b>\n");
        
        Element matchDay = doc.select("div.match-day").first();
        textMessage.append(matchDay.select("span.standard-headline").text()).append("\n");
        
        for (Element match : matchDay.select("table.table")) {
        	
        	long unixTime = Long.parseLong(match.select("div.time").attr("data-unix"));
        	LocalDateTime localTime = LocalDateTime.ofEpochSecond((unixTime/1000)+10800, 0, ZoneOffset.UTC);
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
        	String formattedTime = localTime.format(formatter);
        	
        	
        	textMessage.append(formattedTime)
        	.append("  ");
        	textMessage.append(match.select("div.time").attr("data-unix"))
        	.append(" - ")
        	.append(match.select("div.line-align").get(0).text())
        	.append(" vs ")
        	.append(match.select("div.line-align").get(1).text())
        	.append(" (")
        	.append(match.select("div.map-text").text())
        	.append("), ")
        	.append(match.select("td.event").text())
        	.append("\n");
        	}

        System.out.println(textMessage);

    }
}
