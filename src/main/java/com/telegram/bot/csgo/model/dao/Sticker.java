package com.telegram.bot.csgo.model.dao;

import java.io.Serializable;

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
public class Sticker implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private int id;
	private String sticker;

}
