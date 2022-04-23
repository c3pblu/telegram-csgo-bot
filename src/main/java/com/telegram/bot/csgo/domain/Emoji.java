package com.telegram.bot.csgo.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import lombok.Getter;

@Entity
@Getter
public class Emoji extends IdEntity {

    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 100)
    private String unicode;
}
