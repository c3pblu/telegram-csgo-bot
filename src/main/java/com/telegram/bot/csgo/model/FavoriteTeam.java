package com.telegram.bot.csgo.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "countryCode")
public class FavoriteTeam implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "chat_id")
	private String chatId;

	@Id
	private String name;

	@OneToOne
	@JoinColumn(name = "country_code", unique = true, nullable = false, updatable = true)
	private Flag countryCode;

}
