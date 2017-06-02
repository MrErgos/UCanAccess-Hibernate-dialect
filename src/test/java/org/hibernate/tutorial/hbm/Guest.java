package org.hibernate.tutorial.hbm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class Guest {
	@Id
	@Column(name = "GUEST_ID")
//	@GeneratedValue // (currently not working)
//	@GeneratedValue (strategy = GenerationType.AUTO) // (currently not working)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// previously (before adding IdentityColumnSupport) ...
	// @GenericGenerator(name="Event_AutoNumber_generator", strategy="increment")
	// @GeneratedValue(generator="Event_AutoNumber_generator")
	private Integer id;
	public Integer getId() { return id; }
	@SuppressWarnings("unused")
	private void setId(Integer id) { this.id = id; }

	// limited-length String -> VARCHAR(100) [via Hibernate] -> TEXT(100) [via UCanAccess]
	@Column(length = 100)
	private String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "guests")
	private List<Event> events = new ArrayList<>();
	public List<Event> getEvents() { return this.events; }
	public void setEvents(List<Event> events) { this.events = events; }

	public Guest() { }
	
	public Guest(String name) { this.name = name; }
	
	public Guest(String name, List<Event> events) {
		this.name = name;
		this.events = events;
	}
}
