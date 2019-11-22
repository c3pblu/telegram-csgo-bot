package com.telegram.bot.csgo.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "flags")
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

    public Flag() {

    }

    public Flag(String name, String code, String eCode, String unicode) {
        this.name = name;
        this.code = code;
        this.emojiCode = eCode;
        this.unicode = unicode;
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
        return "[" + this.name + "] [" + this.code + "] [" + this.emojiCode + "] [" + this.unicode + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Flag))
            return false;
        final Flag flag = (Flag) obj;
        if (flag.getCode().equals(this.getCode()))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result;
        result = 29 * this.getCode().hashCode();
        return result;
    }

}
