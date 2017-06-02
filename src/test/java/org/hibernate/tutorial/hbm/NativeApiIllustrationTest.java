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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import antlr.debug.NewLineEvent;
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

		// A SessionFactory is set up once for an application!
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.configure() // configures settings from hibernate.cfg.xml
				.build();
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
	public void testBasicUsage() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Integer maxEventId = (Integer) session.createQuery("select max(id) from Event").uniqueResult();
		Query<?> qry = session.createQuery("delete from Event");
		qry.executeUpdate();
		session.getTransaction().commit();
		session.close();
		
		// create a couple of events...
		session = sessionFactory.openSession();
		session.beginTransaction();
		int eventId1 = (int) session.save(new Event("Our very first event!", sdf.parse("2017-05-21 14:15:16")));
		int eventId2 = (int) session.save(new Event("A follow up event", null));
		assertEquals((maxEventId == null ? 0 : maxEventId) + 2, eventId2);
		session.getTransaction().commit();
		session.close();

		// verify that registerFunction is mapping HQL "current_date()" to Access "Date()"
		// also test concat() HQL function (maps to '+' operator)
		session = sessionFactory.openSession();
		session.beginTransaction();
		qry = session.createQuery(
				"update Event set date=current_date(), title=concat('event', '2'), fee=:newfee where id=" + eventId2);
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
			if (event.getId() == eventId2) {
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
		int hr = (Integer) session.createQuery("select hour(date) from Event where id=" + eventId1).uniqueResult();
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
		String str = session.createQuery("select coalesce(description, title) from Event where id=" + eventId2)
				.uniqueResult().toString();
		assertEquals("event2", str);
		session.getTransaction().commit();
		session.close();

		session = sessionFactory.openSession();
		session.beginTransaction();
		qry = session.createQuery("delete from Guest");
		qry.executeUpdate();
		session.getTransaction().commit();
		session.close();
		
		session = sessionFactory.openSession();
		session.beginTransaction();
		Guest gord = new Guest("Gord");
		session.save(gord);
		List<Event> eventList = session.createQuery("from Event").list();
		assertEquals(2, eventList.size());
//		gord.setEvents(eventList);
//		session.save(gord);
		for (Event evt : eventList) {
			evt.getGuests().add(gord);
//			evt.setGuests(Arrays.asList(gord));
			session.save(evt);
		}
		session.getTransaction().commit();
		session.close();

//		session = sessionFactory.openSession();
//		session.beginTransaction();
////		Event e = (Event) session.createQuery("from Event where id=" + eventId2).uniqueResult();
//		Event e = new Event("Yet another event", null);
//		e.setGuests(Arrays.asList(gord));
//		session.save(e);
//		session.getTransaction().commit();
//		session.close();
	}

}
