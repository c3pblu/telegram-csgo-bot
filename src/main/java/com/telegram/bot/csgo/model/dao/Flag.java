package com.telegram.bot.csgo.model.dao;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "name", "emojiCode", "unicode" })
public class Flag implements Serializable {

	private static final long serialVersionUID = 2L;

	@Column(name = "name")
	private String name;

	@Id
	@Column(name = "code")
	private String code;

	@Column(name = "emoji_code")
	private String emojiCode;

	@Column(name = "unicode")
	private String unicode;

}
