package com.telegram.bot.csgo.adaptor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.dao.FavoriteTeam;
import com.telegram.bot.csgo.model.dao.Flag;
import com.vdurmont.emoji.EmojiParser;

@Component
public class FlagsAdaptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlagsAdaptor.class);

	private Dao dao;

	@Autowired
	public FlagsAdaptor(Dao dao) {
		this.dao = dao;
	}

	public String flagUnicodeFromCountry(String country) {
		String text = null;
		if (country == null) {
			text = EmojiParser.parseToUnicode(":un:");
			LOGGER.debug("Country code: {}, Emoji code: {}", country, ":un: (default)");
			return text;
		}
		Flag ourFlag = new Flag();
		List<Flag> flags = dao.getFlags();
		if (country.matches("[A-Z][A-Z]")) {
			ourFlag = flags.parallelStream().filter(t -> t.getCode().equals(country.toUpperCase())).findFirst()
					.orElse(null);
		} else {
			ourFlag = flags.parallelStream().filter(t -> t.getName().equals(country)).findFirst().orElse(null);
		}

		if (ourFlag != null) {
			if (!StringUtils.isBlank(ourFlag.getUnicode())) {
				text = StringEscapeUtils.unescapeJava(ourFlag.getUnicode());
			} else {
				text = EmojiParser.parseToUnicode(ourFlag.getEmojiCode());
			}
			LOGGER.debug("Country code: {}, Emoji code: {}", country, ourFlag.getEmojiCode());
		}
		if (text == null) {
			text = EmojiParser.parseToUnicode(":un:");
			LOGGER.debug("Country code: {}, Emoji code: {}", country, ":un: (default)");
		}
		return text;
	}

	public String favoriteTeam(String chatId, String name, boolean isBold) {
		String teamName = name;
		FavoriteTeam fvTeam = dao.getTeams(chatId).parallelStream()
				.filter(team -> team.getChatId().equals(chatId) && team.getName().equalsIgnoreCase(name)).findFirst()
				.orElse(null);
		if (fvTeam != null) {
			String flag = flagUnicodeFromCountry(fvTeam.getCountryCode().getCode());
			if (!isBold) {
				teamName = flag + teamName;
			} else {
				teamName = flag + "<b>" + teamName + "</b>";
			}
		}
		return unlinkName(teamName);
	}

	private String unlinkName(String name) {
		if (name.contains(".")) {
			name = name.replace('.', ',');
		}
		return name;
	}

}
