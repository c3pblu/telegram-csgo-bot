package com.telegram.bot.csgo.model.entity;

import lombok.Data;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
public class Emoji {

    @Id
    private String name;
    private String unicode;
}
