package com.telegram.bot.csgo.teams;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "favorite_teams")
@NamedQueries(value = {
        @NamedQuery(name = "favoriteTeams", query = "select p from FavoriteTeam p")
})
public class FavoriteTeam {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    private String name;

    @Column(name = "country_code")
    private String countryCode;

    public FavoriteTeam() {

    }

    public FavoriteTeam(Long chatId, String name, String countryCode) {
        this.chatId = chatId;
        this.name = name;
        this.countryCode = countryCode;

    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    @Override
    public String toString() {
        return "[" + this.chatId + "] [" + this.name + "] [" + this.countryCode + "]";
    }

}
