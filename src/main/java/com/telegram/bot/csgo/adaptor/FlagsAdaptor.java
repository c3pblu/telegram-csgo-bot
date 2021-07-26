package com.telegram.bot.csgo.adaptor;

import com.telegram.bot.csgo.model.entity.FavoriteTeam;
import com.telegram.bot.csgo.model.entity.Flag;
import com.telegram.bot.csgo.repository.FavoriteTeamsRepository;
import com.telegram.bot.csgo.repository.FlagRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class FlagsAdaptor {

    private final FlagRepository flagRepository;
    private final FavoriteTeamsRepository favoriteTeamsRepository;
    private final FlagsAdaptor self;

    @Autowired
    public FlagsAdaptor(FlagRepository flagRepository, FavoriteTeamsRepository favoriteTeamsRepository, @Lazy FlagsAdaptor self) {
        this.flagRepository = flagRepository;
        this.favoriteTeamsRepository = favoriteTeamsRepository;
        this.self = self;
    }

    @Cacheable("flagUnicode")
    public String flagUnicodeFromCountry(String country) {
        String text = null;
        if (StringUtils.isBlank(country)) {
            log.debug("Country code is empty, applying default :un:");
            return EmojiParser.parseToUnicode(":un:");
        }
        Optional<Flag> ourFlag = country.matches("[A-Z][A-Z]")
                ? flagRepository.findByCode(country)
                : flagRepository.findByName(country);
        if (ourFlag.isPresent()) {
            Flag flag = ourFlag.get();
            text = StringUtils.isNotBlank(flag.getUnicode())
                    ? StringEscapeUtils.unescapeJava(flag.getUnicode())
                    : EmojiParser.parseToUnicode(flag.getEmojiCode());
            log.debug("Country code: {}, Emoji code: {}", country, flag.getEmojiCode());
        }
        if (text == null) {
            text = EmojiParser.parseToUnicode(":un:");
            log.debug("Country code: {}, Emoji code: {}", country, ":un: (default)");
        }
        return text;
    }

    public String favoriteTeam(String chatId, String name, boolean isBold) {
        String teamName = name;
        Optional<FavoriteTeam> fvTeam = favoriteTeamsRepository.findByChatIdAndName(chatId, name);
        if (fvTeam.isPresent()) {
            String flag = self.flagUnicodeFromCountry(fvTeam.get().getCountryCode().getCode());
            teamName = isBold ? flag + "<b>" + name + "</b>" : flag + name;
        }
        return unlinkName(teamName);
    }

    private String unlinkName(String name) {
        return name.contains(".") ? name.replace('.', ',') : name;
    }
}
