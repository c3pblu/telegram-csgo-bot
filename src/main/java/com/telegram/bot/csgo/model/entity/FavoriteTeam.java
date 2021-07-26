package com.telegram.bot.csgo.model.entity;

import java.io.Serializable;

import javax.persistence.*;

import lombok.*;

@Entity(name = "favorite_team")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(FavoriteTeamPK.class)
public class FavoriteTeam implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String chatId;
	@Id
	private String name;
	@OneToOne
	@JoinColumn(name = "country_code", unique = true, nullable = false)
	private Flag countryCode;
}
