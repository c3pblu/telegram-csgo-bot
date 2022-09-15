package com.telegram.bot.csgo.service;

import static com.telegram.bot.csgo.helper.MessageHelper.EMPTY_STRING;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;
import com.telegram.bot.csgo.model.message.EmojiCode;
import com.telegram.bot.csgo.repository.EmojiRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmojiService {

    private final EmojiRepo emojiRepo;

    @Cacheable("emoji")
    public String getEmoji(EmojiCode emojiCode) {
        var emoji = emojiRepo.findByName(emojiCode.name());
        return emoji.isPresent()
                ? unescapeJava(emoji.get().getUnicode())
                : emptyString(emojiCode);
    }

    private static String emptyString(EmojiCode emojiCode) {
        log.debug("Emoji '{}' is not found", emojiCode);
        return EMPTY_STRING;
    }
}
