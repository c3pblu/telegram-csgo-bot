package com.telegram.bot.csgo.utils;

import javax.inject.Singleton;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.telegram.bot.csgo.db.FavoriteTeam;
import com.telegram.bot.csgo.db.Flag;

@Component
@Singleton
public class HibernateSessionFactoryUtil {
    private static SessionFactory sessionFactory;

    @Value(value = "${db.connection}")
    private String dbConnection;
    @Value(value = "${db.user}")
    private String user;
    @Value(value = "${db.password}")
    private String password;
    @Value(value = "${db.pool.min_size}")
    private String minSize;
    @Value(value = "${db.pool.max_size}")
    private String maxSize;
    @Value(value = "${db.pool.timeout}")
    private String timeout;
    
    private HibernateSessionFactoryUtil() {
    }

    public SessionFactory getSessionFactory() {

        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration(); 
                configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
                configuration.setProperty("hibernate.connection.url", dbConnection);
                configuration.setProperty("hibernate.connection.username", user);
                configuration.setProperty("hibernate.connection.password", password);
                configuration.setProperty("hibernate.mysql.min_size", minSize);
                configuration.setProperty("hibernate.mysql.max_size", maxSize);
                configuration.setProperty("hibernate.mysql.timeout", timeout);
                configuration.setProperty("hibernate.show_sql","true");
                configuration.addAnnotatedClass(Flag.class);
                configuration.addAnnotatedClass(FavoriteTeam.class);
                StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }
}
