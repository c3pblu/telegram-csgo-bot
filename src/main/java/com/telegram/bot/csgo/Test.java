package com.telegram.bot.csgo;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.telegram.bot.csgo.model.Constants;

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
        GetMethod get = new GetMethod("https://api.twitch.tv/helix/streams?game_id=32399&language=en&language=ru");
        get.setFollowRedirects(true);
        get.setRequestHeader(HttpHeaders.USER_AGENT,
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        get.setRequestHeader("Client-ID", "03fcufq34ixb7h0cb9ap9mr6i9e9y9");
        client.executeMethod(get);
        String response = get.getResponseBodyAsString();
        get.releaseConnection();

        JSONObject json = new JSONObject(response);

        StringBuilder textMessage = new StringBuilder();

        textMessage.append("<b>Live</b>").append(Constants.EMOJI_EXCL_MARK).append(" <b>Streams on Twitch:</b>\n");

        JSONArray arr = json.getJSONArray("data");
        for (int i = 0; i < arr.length(); i++) {
            JSONObject data = arr.getJSONObject(i);
            textMessage

                    .append("<a href=\'https://www.twitch.tv/")
                    .append(data.getString("user_name")).append("\'>")
                    .append(data.getString("user_name"))
                    .append("</a>, ")
                    .append(data.getString("language").toUpperCase())
                    .append(" <b>(")
                    .append(data.getNumber("viewer_count")).append(")</b> ")
                    .append(data.getString("title")).append("\n");

        }

        System.out.println(textMessage);

    }
}
