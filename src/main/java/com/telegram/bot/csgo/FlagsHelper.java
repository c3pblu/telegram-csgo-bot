package com.telegram.bot.csgo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import com.vdurmont.emoji.EmojiParser;

public class FlagsHelper {

	private SessionFactory sessionFactory;

	public void setup() {

		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		} catch (Exception ex) {
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	public void exit() {
		sessionFactory.close();
	}

	public void read() {
		Session session = sessionFactory.openSession();

		session.close();
	}



	public static String getFlgUnicode(String countryName) {
		return EmojiParser.parseToUnicode(":by:");
	}

}
