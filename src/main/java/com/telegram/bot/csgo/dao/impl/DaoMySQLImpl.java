package com.telegram.bot.csgo.dao.impl;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.telegram.bot.csgo.dao.Dao;
import com.telegram.bot.csgo.model.dao.FavoriteTeam;
import com.telegram.bot.csgo.model.dao.Flag;
import com.telegram.bot.csgo.model.dao.Result;
import com.telegram.bot.csgo.model.dao.Sticker;

@Repository
public class DaoMySQLImpl implements Dao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	@CacheEvict(cacheNames = "teams", allEntries = true)
	public Result updateOrSaveTeam(String chatId, String name, String countryCode) {
		Optional<Flag> newFtFlagOpt = getFlags().stream().filter(flag -> flag.getCode().equals(countryCode))
				.findFirst();
		if (!newFtFlagOpt.isPresent()) {
			return Result.FLAG_NOT_FOUND;
		}
		Flag newFtFlag = newFtFlagOpt.get();
		FavoriteTeam newFt = new FavoriteTeam(chatId, name, newFtFlag);
		if (isTeamPresents(chatId, newFt)) {
			boolean isSameFlag = getTeams(chatId).stream().filter(team -> team.equals(newFt))
					.anyMatch(team -> team.getCountryCode().equals(newFtFlag));
			if (isSameFlag) {
				return Result.ALREADY_EXIST;
			}
			entityManager.merge(newFt);
			return Result.UPDATED;
		} else {
			entityManager.persist(newFt);
			return Result.INSERTED;
		}
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = "teams", allEntries = true)
	public Result deleteTeam(String chatId, String name) {
		FavoriteTeam teamToDelete = new FavoriteTeam(chatId, name, null);
		if (!isTeamPresents(chatId, teamToDelete)) {
			return Result.NOTHING_WAS_CHANGED;
		}
		entityManager.remove(entityManager.contains(teamToDelete) ? teamToDelete : entityManager.merge(teamToDelete));
		return Result.DELETED;
	}

	private boolean isTeamPresents(String chatId, FavoriteTeam teamToCheck) {
		if (getTeams(chatId).stream().anyMatch(team -> team.equals(teamToCheck))) {
			return true;
		}
		return false;
	}

	@Override
	@Cacheable("stickers")
	public List<Sticker> getStickers() {
		return entityManager.createQuery("select p from Sticker p", Sticker.class).getResultList();
	}

	@Override
	@Cacheable("flags")
	public List<Flag> getFlags() {
		return entityManager.createQuery("select p from Flag p", Flag.class).getResultList();
	}

	@Override
	@Cacheable("teams")
	public List<FavoriteTeam> getTeams(String chatId) {
		return entityManager.createQuery("select p from FavoriteTeam p where p.chatId = ?1", FavoriteTeam.class)
				.setParameter(1, chatId).getResultList();
	}

}
