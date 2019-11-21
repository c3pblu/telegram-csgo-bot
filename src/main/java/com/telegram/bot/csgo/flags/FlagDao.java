package com.telegram.bot.csgo.flags;

import java.util.List;

import javax.annotation.PostConstruct;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import com.telegram.bot.csgo.teams.FavoriteTeam;
import com.telegram.bot.csgo.utils.HibernateSessionFactoryUtil;

//@Component
public class FlagDao {

    private static List<Flag> flags;

    private static List<FavoriteTeam> favoriteTeams;

    public static List<Flag> getFlags() {
        return flags;
    }

    public static void setFlags(List<Flag> flags) {
        FlagDao.flags = flags;
    }

    public static List<FavoriteTeam> getFavoriteTeams() {
        return favoriteTeams;
    }

    public static void setFavoriteTeams(List<FavoriteTeam> favoriteTeams) {
        FlagDao.favoriteTeams = favoriteTeams;
    }

//    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        flags = session.createQuery("select p from Flag p").list();
        System.out.println(":::::::::::" + getFlags().toString());
        favoriteTeams = session.createQuery("select p from FavoriteTeam p").list();
        System.out.println(":::::::::::" + getFavoriteTeams().toString());

    }

}
