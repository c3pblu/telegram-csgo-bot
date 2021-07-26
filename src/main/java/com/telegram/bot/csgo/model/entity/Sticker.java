package com.telegram.bot.csgo.model.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;

@Entity
@Getter
public class Sticker implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;
	private String sticker;

}
