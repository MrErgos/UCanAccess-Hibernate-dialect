/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.tutorial.hbm;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import junit.framework.TestCase;

/**
 * Illustrates use of Hibernate native APIs.
 *
 * @author Steve Ebersole
 */
public class NativeApiIllustrationTest extends TestCase {
	private SessionFactory sessionFactory;

	@Override
	protected void setUp() throws Exception {
		Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
		
		StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder()
				.configure(); // configures settings from hibernate.cfg.xml
		// allow tester to specify their own connection URL (via -D JVM argument)
		String runtimeUrl = System.getProperty("HIBERNATE_CONNECTION_URL"); 
		if (runtimeUrl != null) {
			ssrb.applySetting("hibernate.connection.url", runtimeUrl);
		}
		final StandardServiceRegistry registry = ssrb.build();
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		} catch (Exception e) {
			// The registry would be destroyed by the SessionFactory, but we had
			// trouble building the SessionFactory so destroy it manually.
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void testBasicUsage() throws ParseException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// create a couple of events...
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Event e1 = new Event("Our very first event!", sdf.parse("2017-05-21 14:15:16"));
		InputStream inStream = NativeApiIllustrationTest.class.getClassLoader().getResourceAsStream("logo_96.png");
		e1.setLogo(IOUtils.toByteArray(inStream));
		session.save(e1);
		int new_id = (int) session.save(new Event("A follow up event", null));
		assertEquals(2, new_id);
		session.getTransaction().commit();
		session.close();

		// verify that registerFunction is mapping HQL "current_date()" to Access "Date()"
		// also test concat() HQL function (maps to '+' operator)
		session = sessionFactory.openSession();
		session.beginTransaction();
		Query<?> qry = session.createQuery(
				"update Event set date=current_date(), title=concat('event', '2'), fee=:newfee where id=2");
		qry.setParameter("newfee", new BigDecimal("123.45"));
		qry.executeUpdate();
		session.getTransaction().commit();
		session.close();

		// now lets pull events from the database and list them
		session = sessionFactory.openSession();
		session.beginTransaction();
		List<Event> resultList = session.createQuery("from Event").list();
		for (Event event : (List<Event>) resultList) {
			System.out.println("Event (" + event.getDate() + ") : " + event.getTitle());
			if (event.getId() == 2) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(event.getDate());
				assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0
						&& cal.get(Calendar.SECOND) == 0);
			}
		}
		session.getTransaction().commit();
		session.close();

		// test re-mapping of hour() and related HQL functions
		session = sessionFactory.openSession();
		session.beginTransaction();
		int hr = (Integer) session.createQuery("select hour(date) from Event where id=1").uniqueResult();
		assertEquals(14, hr);
		session.getTransaction().commit();
		session.close();

		// like operator
		session = sessionFactory.openSession();
		session.beginTransaction();
		resultList = session.createQuery("select id from Event where title like 'event%'").list();
		assertEquals(1, resultList.size());
		session.getTransaction().commit();
		session.close();

		// != operator
		session = sessionFactory.openSession();
		session.beginTransaction();
		resultList = session.createQuery("select id from Event where title != 'event2'").list();
		assertEquals(1, resultList.size());
		session.getTransaction().commit();
		session.close();

		// coalesce function
		session = sessionFactory.openSession();
		session.beginTransaction();
		String str = session.createQuery("select coalesce(description, title) from Event where id=2").uniqueResult()
				.toString();
		assertEquals("event2", str);
		session.getTransaction().commit();
		session.close();

	}

}
