package com.telegram.bot.csgo.model.entity;

import java.io.Serializable;

import javax.persistence.*;

import lombok.*;

@Entity
@Getter
public class Flag implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	@Id
	private String code;
	private String emojiCode;
	private String unicode;
}
