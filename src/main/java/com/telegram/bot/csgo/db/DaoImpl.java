package com.telegram.bot.csgo.db;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.telegram.bot.csgo.utils.HibernateSessionFactoryUtil;

@Component
@SuppressWarnings("unchecked")
public class DaoImpl {
	
	@Autowired
	private HibernateSessionFactoryUtil hibernate;

    private static List<Flag> flags;
    private static List<FavoriteTeam> teams;

    public List<Flag> getFlags() {
        return flags;
    }

    public List<FavoriteTeam> getTeams() {
        return teams;
    }

    @PostConstruct
    public void init() {
        fillAllFlags();
        fillAllTeams();
    }

    public DbResult updateOrSaveTeam(Long chatId, String name, String countryCode) {
        Flag newFtFlag = flags.parallelStream().filter(flag -> flag.getCode().equals(countryCode)).findFirst().orElse(null);
        if (newFtFlag == null)
            return DbResult.NOT_FOUND;
        FavoriteTeam newFt = new FavoriteTeam(chatId, name, newFtFlag);

        boolean isSameTeam = newFt
                .equals(teams.parallelStream().filter(team -> team.equals(newFt)).findFirst().orElse(null));

        Session sessionTwo = hibernate.getSessionFactory().openSession();
        Transaction transaction = sessionTwo.beginTransaction();

        if (isSameTeam) {
            boolean isSameFlag = newFtFlag.equals(
                    teams.parallelStream().filter(team -> team.equals(newFt)).findFirst().get().getCountryCode());
            if (isSameFlag)
                return DbResult.ALREADY_EXIST;
            Query query = sessionTwo.createNamedQuery("updateFavoriteTeam")
                    .setParameter("countryCode", newFt.getCountryCode())
                    .setParameter("name", newFt.getName())
                    .setParameter("chatId", newFt.getChatId());
            int count = query.executeUpdate();
            transaction.commit();
            sessionTwo.close();
            if (count > 0) {
                fillAllTeams();
                return DbResult.UPDATED;
            } else
                return DbResult.NOTHING_WAS_CHANGED;

        } else {
            sessionTwo.save(newFt);
            transaction.commit();
            sessionTwo.close();
            fillAllTeams();
            return DbResult.INSERTED;

        }
    }

    public DbResult deleteTeam(Long chatId, String name) {
        Session session = hibernate.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createNamedQuery("deleteFavoriteTeam")
                .setParameter("name", name)
                .setParameter("chatId", chatId);
        int count = query.executeUpdate();
        transaction.commit();
        session.close();
        if (count > 0) {
            fillAllTeams();
            return DbResult.DELETED;
        } else
            return DbResult.NOTHING_WAS_CHANGED;
    }

    private void fillAllFlags() {
        Session session = hibernate.getSessionFactory().openSession();
        flags = session.createQuery("select p from Flag p").list();
        session.close();
    }

    private void fillAllTeams() {
        Session session = hibernate.getSessionFactory().openSession();
        teams = session.createQuery("select p from FavoriteTeam p").list();
        session.close();
    }

}
