package com.telegram.bot.csgo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class MessageHelper {

	public static SendMessage topTeams(Integer count) {
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod("https://www.hltv.org/ranking/teams");
		get.setFollowRedirects(true);
		get.setRequestHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
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
			textMessage.append("<b>").append(header.text()).append("</b>\n\n");
			for (Element team : rankedTeams) {
				if (team.select("span.position").text().equals("#" + (count + 1))) {
					break;
				}
				StringBuilder row = new StringBuilder();
				row.append("<a href=\'").append(team.select("div.ranking-header").select("img").attr("src")).append("\'></a>");
				row.append("<b>").append(team.select("span.position").text()).append("</b> (");
				row.append(team.select("div.change").text()).append(") ");
				row.append("<a href=\'https://hltv.org")
						.append(team.select("div.more").select("a[class=details moreLink]").attr("href")).append("\'>")
						.append(team.select("span.name").text()).append("</a> ");
				row.append(team.select("span.points").text()).append(" [");
				ArrayList<String> listPlayers = new ArrayList<>();
				for (Element player : team.select("div.rankingNicknames")) {
					listPlayers.add(player.text());
				}
				row.append(String.join(", ", listPlayers));
				row.append("]\n");
				textMessage.append(row);
			}
			sendMessage.setText(textMessage.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
			sendMessage.setText("Не смог получить данные с сайта...");
		}
		return sendMessage;
	}

	public static SendMessage topPlayers(Integer count) {
		HttpClient client = new HttpClient();
		String year = String.valueOf(LocalDate.now().getYear());
		GetMethod get = new GetMethod(
				"https://www.hltv.org/stats/players?startDate=" + year + "-01-01&endDate=" + year + "-12-31");
		get.setFollowRedirects(true);
		get.setRequestHeader(HttpHeaders.USER_AGENT,
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
		SendMessage sendMessage = new SendMessage();
		sendMessage.setParseMode("html");
		try {
			client.executeMethod(get);
			String response = get.getResponseBodyAsString();
			get.releaseConnection();
			Document doc = Jsoup.parse(response);
			Elements rows = doc.select("tr");
			StringBuilder textMessage = new StringBuilder();
			textMessage.append("<b>CS:GO World Top Players ").append(year).append("</b>\n\n");
			textMessage.append("<b>").append(doc.select("tr.stats-table-row").text()).append("</b>\n");

			int number = 1;
			for (Element value : rows) {
				if (value.select("td.statsDetail").first() == null) {
					continue;
				}
				if (number > count) {
					break;
				}
				textMessage.append("<b>#").append(number).append("</b> ");
				textMessage.append("<a href=\'https://hltv.org")
						.append(value.select("td.playerCol").select("a").attr("href")).append("\'>")
						.append(value.select("td.playerCol").text()).append("</a>, ");
				textMessage.append(value.select("td.teamCol").select("img").attr("title")).append(", ");
				textMessage.append(value.select("td.statsDetail").get(0).text()).append(", ");
				textMessage.append(value.select("td.kdDiffCol").text()).append(", ");
				textMessage.append(value.select("td.statsDetail").get(1).text()).append(", ");
				textMessage.append(value.select("td.ratingCol").text()).append("\n");
				number++;
			}
			sendMessage.setText(textMessage.toString());
			
		} catch (IOException e) {
			sendMessage.setText("Не смог получить данные с сайта...");

		}
		return sendMessage;
	}

	public static SendMessage help() {
		SendMessage help = new SendMessage();
		help.setParseMode("markdown");
		help.setText("\u2139 Могу посмотреть, что там нового на HLTV.org\n" 
				+ "Спрашивай, не стесняйся:\n"
				+ "\u2705 *.хелп* - Эта информация\n" 
				+ "\u2705 *.меню* - Меню\n"
				+ "\u2705 *.топ10* - Top 10 Команд\n"
				+ "\u2705 *.топ20* - Top 20 Команд\n" 
				+ "\u2705 *.топ30* - Top 30 Команд\n"
				+ "\u2705 *.топ10игроков* - Top 10 Игроков\n" 
				+ "\u2705 *.топ20игроков* - Top 20 Игроков\n"
				+ "\u2705 *.топ30игроков* - Top 30 Игроков");
		return help;
	}

	public static SendMessage toBot(String who) {
		SendMessage message = new SendMessage();
		Map<Integer, String> frases = new HashMap<>();
		frases.put(0, "Ну все... Молись @" + who + " сейчас отхватишь! \uD83D\uDCAA");
		frases.put(1, "Го 1 на 1 на квартиру @" + who + " или засцал?! \uD83D\uDCAA");
		message.setText(frases.get(new Random().nextInt(1 + 1)));
		return message;
	}

}
