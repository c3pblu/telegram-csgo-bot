package com.telegram.bot.csgo.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.telegram.bot.csgo.model.DbResult;
import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.model.Flag;
import com.telegram.bot.csgo.model.Sticker;

@Repository
public class DaoMySQLImpl implements Dao {

	@PersistenceUnit
	private EntityManagerFactory factory;
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	@CacheEvict(value = "teams", allEntries = true)
	public DbResult updateOrSaveTeam(Long chatId, String name, String countryCode) {
		DbResult result = null;
		Flag newFtFlag = getFlags().parallelStream().filter(flag -> flag.getCode().equals(countryCode)).findFirst()
				.orElse(null);
		if (newFtFlag == null) {
			return DbResult.FLAG_NOT_FOUND;
		}
		FavoriteTeam newFt = new FavoriteTeam(chatId, name, newFtFlag);
		if (isTeamPresents(newFt)) {
			boolean isSameFlag = newFtFlag.equals(
					getTeams().parallelStream().filter(team -> team.equals(newFt)).findFirst().get().getCountryCode());
			if (isSameFlag) {
				return DbResult.ALREADY_EXIST;
			}
			entityManager.merge(newFt);
			result = DbResult.UPDATED;
		} else {
			entityManager.persist(newFt);
			result = DbResult.INSERTED;

		}
		return result;
	}

	@Override
	@Transactional
	@CacheEvict(value = "teams", allEntries = true)
	public DbResult deleteTeam(Long chatId, String name) {
		FavoriteTeam teamToDelete = new FavoriteTeam(chatId, name, null);
		if (!isTeamPresents(teamToDelete)) {
			return DbResult.NOTHING_WAS_CHANGED;
		}
		entityManager.remove(entityManager.contains(teamToDelete) ? teamToDelete : entityManager.merge(teamToDelete));
		return DbResult.DELETED;
	}

	private boolean isTeamPresents(FavoriteTeam teamToCheck) {
		if (getTeams().stream().anyMatch(team -> team.equals(teamToCheck))) {
			return true;
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Cacheable(value = "stickers")
	public List<Sticker> getStickers() {
		return entityManager.createQuery("select p from Sticker p").getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	@Cacheable(value = "flags")
	public List<Flag> getFlags() {
		return entityManager.createQuery("select p from Flag p").getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	@Cacheable(value = "teams")
	public List<FavoriteTeam> getTeams() {
		return entityManager.createQuery("select p from FavoriteTeam p").getResultList();
	}

}