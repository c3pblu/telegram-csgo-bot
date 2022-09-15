package com.telegram.bot.csgo.repository;

import java.util.Optional;
import com.telegram.bot.csgo.domain.Emoji;
import org.springframework.data.repository.CrudRepository;

public interface EmojiRepo extends CrudRepository<Emoji, Integer> {

    Optional<Emoji> findByName(String name);
}
