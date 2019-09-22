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
import java.util.ArrayList;

public class Test {
    public static void main(String args[]) throws IOException {
        HttpClient client = new HttpClient();
        String year = String.valueOf(LocalDate.now().getYear());
        GetMethod get = new GetMethod("https://www.hltv.org/stats/players?startDate=" + year + "-01-01&endDate=" + year + "-12-31");
        get.setFollowRedirects(true);
        get.setRequestHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        client.executeMethod(get);
        String response = get.getResponseBodyAsString();
        get.releaseConnection();

        Document doc = Jsoup.parse(response);
        Elements rows = doc.select("tr");

        StringBuilder textMessage = new StringBuilder();

        for (Element value : rows) {
            System.out.println(value.select("td.playerCol").text());
            System.out.println(value.select("td.statsDetail").text());

        }

    }
}
