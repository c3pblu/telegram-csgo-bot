package com.telegram.bot.csgo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;

@Entity
@Getter
public class Sticker extends IdEntity {

    @Column(nullable = false, length = 100)
    private String code;
}
