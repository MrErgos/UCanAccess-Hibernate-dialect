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
package net.ucanaccess.hibernate.dialect.test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
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
        Logger myLogger = Logger.getLogger("org.hibernate"); 
        myLogger.setLevel(Level.SEVERE);
        
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
            myLogger.log(Level.SEVERE, e.getMessage());
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
        Event e1 = new Event("Our very first event!", LocalDateTime.of(2017, 5, 21, 14, 15, 16));
        InputStream inStream = NativeApiIllustrationTest.class.getClassLoader().getResourceAsStream("logo_96.png");
        e1.setLogo(IOUtils.toByteArray(inStream));
        int eventId1 = (int) session.save(e1);
        int eventId2 = (int) session.save(new Event("A follow up event", null));
        assertEquals((maxEventId == null ? 0 : maxEventId) + 2, eventId2);
        session.getTransaction().commit();
        session.close();

        // verify that registerFunction is mapping HQL "current_date()" to Access "Date()"
        // also test concat() HQL function (maps to '+' operator)
        session = sessionFactory.openSession();
        session.beginTransaction();
        qry = session.createQuery(
                "update Event set date=current_date(), title=concat('event', '2'), fee=:newfee where id=:id2");
        qry.setParameter("newfee", new BigDecimal("123.45"));
        qry.setParameter("id2", eventId2);
        qry.executeUpdate();
        session.getTransaction().commit();
        session.close();

        // now let's pull events from the database and list them
        session = sessionFactory.openSession();
        session.beginTransaction();
        List<Event> resultList = session.createQuery("from Event").list();
        for (Event event : resultList) {
            System.out.println("Event (" + event.getDate() + ") : " + event.getTitle());
            if (event.getId() == eventId2) {
                LocalDateTime ldt = event.getDate(); 
                assertTrue(ldt.getHour() == 0 && ldt.getMinute() == 0 && ldt.getSecond() == 0);
            }
        }
        session.getTransaction().commit();
        session.close();

        // test re-mapping of hour() and related HQL functions
        session = sessionFactory.openSession();
        session.beginTransaction();
        qry = session.createQuery("select hour(date) from Event where id=:id1");
        qry.setParameter("id1", eventId1);
        int hr = (Integer) qry.uniqueResult();
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
        qry = session.createQuery("select coalesce(description, title) from Event where id=:id2");
        qry.setParameter("id2", eventId2);
        String str = qry.uniqueResult().toString();
        assertEquals("event2", str);
        session.getTransaction().commit();
        session.close();

        // range query
        session = sessionFactory.openSession();
        session.beginTransaction();
        Query<?> rangeQry = session.createQuery("from Event order by id");
        rangeQry.setFirstResult(1);  // zero-based
        rangeQry.setMaxResults(1);
        resultList = (List<Event>) rangeQry.list();
        assertEquals(1, resultList.size());
        assertEquals("event2", resultList.get(0).getTitle());
        session.getTransaction().commit();
        session.close();

        // count(distinct ...) query
        session = sessionFactory.openSession();
        session.beginTransaction();
        Query<?> countDistinctQry = session.createQuery("select count(distinct title) from Event");
        long distinctCount = (long) countDistinctQry.uniqueResult();
        assertEquals(2, distinctCount);
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
        Guest gord = new Guest("gord@example.com", "Gord");
        session.save(gord);
        List<Event> eventList = session.createQuery("from Event").list();
        assertEquals(2, eventList.size());
        for (Event evt : eventList) {
            evt.getGuests().add(gord);  // incremental add
            session.save(evt);
        }
        session.getTransaction().commit();
        session.close();

        session = sessionFactory.openSession();
        session.beginTransaction();
        Guest anne = new Guest("anne@example.com", "Anne");
        anne.setVip(true);
        session.save(anne);
        Event evt3 = new Event("Yet another event", null);
        evt3.setGuests(Arrays.asList(new Guest[] { gord, anne }));  // whole new list
        int eventId3 = (int) session.save(evt3);
        session.getTransaction().commit();
        //
        // test IN clause with list
        qry = session.createQuery("from Event where id in :id_list");
        qry.setParameter("id_list", Arrays.asList(new Integer[] { eventId1, eventId3 }));
        eventList = (List<Event>) qry.list();
        assertEquals(2, eventList.size());
        //
        // list Guests for a particular Event
        for (Guest gst : evt3.getGuests()) {
            System.out.printf("%s (VIP: %s)%n", gst.getName(), gst.getVip());
        }
        session.close();
    }

}
