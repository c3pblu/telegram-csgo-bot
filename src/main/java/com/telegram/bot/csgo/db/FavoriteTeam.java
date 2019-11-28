package com.telegram.bot.csgo.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "favorite_teams")
@NamedQueries(value = {
        @NamedQuery(name = "updateFavoriteTeam", query = "update FavoriteTeam p set p.countryCode = :countryCode where p.chatId = :chatId and p.name = :name"),
        @NamedQuery(name = "deleteFavoriteTeam", query = "delete from FavoriteTeam p where p.chatId = :chatId and p.name = :name")
})
public class FavoriteTeam implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Id
    private String name;

    @OneToOne
    @JoinColumn(name = "country_code", unique = true, nullable = false, updatable = false)
    private Flag countryCode;

    public FavoriteTeam() {

    }

    public FavoriteTeam(Long chatId, String name, Flag countryCode) {
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

    public Flag getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Flag countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public String toString() {
        return "[" + this.chatId + "] [" + this.name + "] [" + this.countryCode + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FavoriteTeam))
            return false;
        final FavoriteTeam fvTeam = (FavoriteTeam) obj;
        if (fvTeam.getChatId().equals(this.getChatId())
                && fvTeam.getName().equals(this.getName()))
            return true;
        return false;
    }

}
