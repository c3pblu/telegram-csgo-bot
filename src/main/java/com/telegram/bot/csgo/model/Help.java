package com.telegram.bot.csgo.model;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class Help extends SendMessage {


	public Help() {
		this.setText("Available comands:\r\n" + 
				"/help - all commads description\r\n" + 
				"/top10 - Top 10 Teams\r\n" + 
				"/top100 - Top 100  Teams\r\n" + 
				"/top10players - Top 100 Players");
	}
	
	
}
