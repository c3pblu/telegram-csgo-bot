package com.telegram.bot.csgo.repository;


import com.telegram.bot.csgo.model.entity.Emoji;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmojiRepository extends CrudRepository<Emoji, String> {

    @Cacheable("emoji")
    default String getEmoji(String code) {
        Optional<Emoji> emoji = findById(code);
        return emoji.isPresent() ? StringEscapeUtils.unescapeJava(emoji.get().getUnicode()) : "";
    }
}
