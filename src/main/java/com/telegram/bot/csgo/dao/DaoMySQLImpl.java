package com.telegram.bot.csgo.dao;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.telegram.bot.csgo.model.DbResult;
import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.model.Flag;

@Repository
@ManagedResource
public class DaoMySQLImpl implements Dao {

	@PersistenceUnit
	private EntityManagerFactory factory;
	@PersistenceContext
	private EntityManager entityManager;

	private static List<Flag> flags;
	private static List<FavoriteTeam> teams;

	@PostConstruct
	private void init() {
		fillAllFlags();
		fillAllTeams();
	}

	@Override
	@Transactional
	public DbResult updateOrSaveTeam(Long chatId, String name, String countryCode) {
		DbResult result = null;
		Flag newFtFlag = flags.parallelStream().filter(flag -> flag.getCode().equals(countryCode)).findFirst()
				.orElse(null);
		if (newFtFlag == null) {
			return DbResult.FLAG_NOT_FOUND;
		}
		FavoriteTeam newFt = new FavoriteTeam(chatId, name, newFtFlag);
		if (isTeamPresents(newFt)) {
			boolean isSameFlag = newFtFlag.equals(
					teams.parallelStream().filter(team -> team.equals(newFt)).findFirst().get().getCountryCode());
			if (isSameFlag) {
				return DbResult.ALREADY_EXIST;
			}
			entityManager.merge(newFt);
			result = DbResult.UPDATED;
		} else {
			entityManager.persist(newFt);
			result = DbResult.INSERTED;

		}
		fillAllTeams();
		return result;
	}

	@Override
	@Transactional
	public DbResult deleteTeam(Long chatId, String name) {
		FavoriteTeam teamToDelete = new FavoriteTeam(chatId, name, null);
		if (!isTeamPresents(teamToDelete)) {
			return DbResult.NOTHING_WAS_CHANGED;
		}
		entityManager.remove(entityManager.contains(teamToDelete) ? teamToDelete : entityManager.merge(teamToDelete));
		fillAllTeams();
		return DbResult.DELETED;
	}

	private boolean isTeamPresents(FavoriteTeam teamToCheck) {
		if (teams.stream().anyMatch(team -> team.equals(teamToCheck))) {
			return true;
		}
		return false;
	}

	@Override
	@ManagedOperation
	@SuppressWarnings("unchecked")
	public void fillAllFlags() {
		flags = entityManager.createQuery("select p from Flag p").getResultList();
	}

	@Override
	@ManagedOperation
	@SuppressWarnings("unchecked")
	public void fillAllTeams() {
		teams = entityManager.createQuery("select p from FavoriteTeam p").getResultList();
	}

	@Override
	public List<Flag> getFlags() {
		return flags;
	}

	@Override
	public List<FavoriteTeam> getTeams() {
		return teams;
	}

}
