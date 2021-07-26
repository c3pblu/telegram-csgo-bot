package com.telegram.bot.csgo.model.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class FavoriteTeamPK implements Serializable {
    private static final long serialVersionUID = 1L;
    private String chatId;
    private String name;
}
