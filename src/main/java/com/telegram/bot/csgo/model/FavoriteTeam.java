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
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteTeam implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "chat_id")
	private Long chatId;

	@Id
	private String name;

	@OneToOne
	@JoinColumn(name = "country_code", unique = true, nullable = false, updatable = true)
	private Flag countryCode;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FavoriteTeam))
			return false;
		final FavoriteTeam fvTeam = (FavoriteTeam) obj;
		if (fvTeam.getChatId().equals(this.getChatId()) && fvTeam.getName().equals(this.getName()))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int result;
		result = 29 * this.getChatId().hashCode();
		return result;
	}

}
