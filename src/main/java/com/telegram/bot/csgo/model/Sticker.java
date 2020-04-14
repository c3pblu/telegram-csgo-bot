package com.telegram.bot.csgo.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stickers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sticker {

	@Id
	private int id;
	private String sticker;

}
