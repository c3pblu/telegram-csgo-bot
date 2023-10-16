package com.telegram.bot.csgo.domain;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;

@Entity
@Getter
public class Flag extends IdEntity {

    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 4)
    private String code;
    @Column(name = "emoji_code", nullable = false, length = 4)
    private String emojiCode;
    @Column(nullable = true, length = 100)
    private String unicode;

    @OneToMany(mappedBy = "flag", fetch = FetchType.LAZY)
    private Set<FavoriteTeam> favoriteTeam;
}
