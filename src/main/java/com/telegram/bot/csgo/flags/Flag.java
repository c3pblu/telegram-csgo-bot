package com.telegram.bot.csgo.flags;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.telegram.bot.csgo.teams.FavoriteTeam;

@Entity
@Table(name = "flags")
@NamedQueries(value = {
        @NamedQuery(name = "byNameAllFileds", query = "select p from Flag p where p.name = :name"),
        @NamedQuery(name = "byNameOneField", query = "select p.name from Flag p where p.name = :name"),
        @NamedQuery(name = "byCode", query = "from Flag where code = :code"),
        @NamedQuery(name = "byEmojiCode", query = "from Flag where emojiCode = :emojiCode"),
})
public class Flag implements Serializable {

    private static final long serialVersionUID = -1592519970849703869L;

    @Column(name = "name")
    private String name;

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "emoji_code")
    private String emojiCode;

    @Column(name = "unicode")
    private String unicode;

    public Flag() {

    }

    public Flag(String name, String code, String eCode) {
        this.name = name;
        this.code = code;
        this.emojiCode = eCode;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmojiCode() {
        return emojiCode;
    }

    public void setEmojiCode(String eCode) {
        this.emojiCode = eCode;
    }
    
    public String getUnicode() {
        return unicode;
    }

    public void setUnicode(String unicode) {
        this.unicode = unicode;
    }

    @Override
    public String toString() {
        return "[" + this.name + "] [" + this.code + "] [" + this.emojiCode + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Flag))
            return false;
        final Flag flag = (Flag) obj;
        if (flag.getCode().equals(this.getCode())
                && flag.getEmojiCode().equals(this.getEmojiCode())
                && flag.getName().equals(this.getName()))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        result = this.getCode().hashCode();
        result = 29 * result + this.getEmojiCode().hashCode();
        return result;
    }

}
