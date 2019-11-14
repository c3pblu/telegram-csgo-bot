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

        Element matchDay = doc.select("div.match-day").first();
        
        for (Element a : matchDay.select("a")) {
        	System.out.println(a.attr("href"));
        }
        

    }
}
