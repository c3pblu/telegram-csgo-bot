package com.telegram.bot.csgo.service;

import java.util.Optional;
import java.util.regex.Pattern;
import static com.vdurmont.emoji.EmojiParser.parseToUnicode;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;
import com.telegram.bot.csgo.domain.Flag;
import com.telegram.bot.csgo.repository.FlagRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlagService {

    private static final String DEFAULT_COUNTRY_FLAG = ":un:";
    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("[A-Z][A-Z]");

    private final FlagRepo flagRepo;

    public Optional<Flag> getOneByCountryCode(String countryCode) {
        return flagRepo.findByCode(countryCode);
    }

    @Cacheable("flagUnicode")
    public String flagUnicodeFromCountry(String country) {
        if (isBlank(country)) {
            log.debug("Country code is empty, applying default {}", DEFAULT_COUNTRY_FLAG);
            return parseToUnicode(DEFAULT_COUNTRY_FLAG);
        }
        var foundFlag = COUNTRY_CODE_PATTERN.matcher(country).matches()
                ? flagRepo.findByCode(country)
                : flagRepo.findByName(country);
        String text = null;
        if (foundFlag.isPresent()) {
            var flag = foundFlag.get();
            text = isNotBlank(flag.getUnicode())
                    ? unescapeJava(flag.getUnicode())
                    : parseToUnicode(flag.getEmojiCode());
            log.debug("Country code: {}, Emoji code: {}", country, flag.getEmojiCode());
        }
        if (text == null) {
            text = parseToUnicode(DEFAULT_COUNTRY_FLAG);
            log.debug("Country code: {}, Emoji code: {}", country, DEFAULT_COUNTRY_FLAG + " (default)");
        }
        return text;
    }
}
