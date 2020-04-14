package com.telegram.bot.csgo.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flags")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
