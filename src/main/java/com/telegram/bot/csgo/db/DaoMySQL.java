package com.telegram.bot.csgo.db;

import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.telegram.bot.csgo.model.DbResult;
import com.telegram.bot.csgo.model.FavoriteTeam;
import com.telegram.bot.csgo.model.Flag;

@Component
@ManagedResource
public class DaoMySQL implements Dao {

	@Autowired
	private HibernateSessionFactory hibernate;

	private static List<Flag> flags;
	private static List<FavoriteTeam> teams;

	public List<Flag> getFlags() {
		return flags;
	}

	public List<FavoriteTeam> getTeams() {
		return teams;
	}

	@PostConstruct
	private void init() {
		fillAllFlags();
		fillAllTeams();
	}

	@Override
	public DbResult updateOrSaveTeam(Long chatId, String name, String countryCode) {
		DbResult result = null;
		Flag newFtFlag = flags.parallelStream().filter(flag -> flag.getCode().equals(countryCode)).findFirst()
				.orElse(null);
		if (newFtFlag == null) {
			return DbResult.FLAG_NOT_FOUND;
		}
		FavoriteTeam newFt = new FavoriteTeam(chatId, name, newFtFlag);
		Session sessionTwo = hibernate.getSessionFactory().openSession();
		Transaction transaction = sessionTwo.beginTransaction();
		if (isTeamPresents(newFt)) {
			boolean isSameFlag = newFtFlag.equals(
					teams.parallelStream().filter(team -> team.equals(newFt)).findFirst().get().getCountryCode());
			if (isSameFlag) {
				return DbResult.ALREADY_EXIST;
			}
			sessionTwo.update(newFt);
			result = DbResult.UPDATED;
		} else {
			sessionTwo.save(newFt);
			result = DbResult.INSERTED;

		}
		transaction.commit();
		sessionTwo.close();
		fillAllTeams();
		return result;
	}

	@Override
	public DbResult deleteTeam(Long chatId, String name) {
		FavoriteTeam teamToDelete = new FavoriteTeam(chatId, name, null);
		if (!isTeamPresents(teamToDelete)) {
			return DbResult.NOTHING_WAS_CHANGED;
		}
		Session session = hibernate.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(teamToDelete);
		transaction.commit();
		session.close();
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
		Session session = hibernate.getSessionFactory().openSession();
		flags = session.createQuery("select p from Flag p").list();
		session.close();
	}

	@Override
	@ManagedOperation
	@SuppressWarnings("unchecked")
	public void fillAllTeams() {
		Session session = hibernate.getSessionFactory().openSession();
		teams = session.createQuery("select p from FavoriteTeam p").list();
		session.close();
	}

}
