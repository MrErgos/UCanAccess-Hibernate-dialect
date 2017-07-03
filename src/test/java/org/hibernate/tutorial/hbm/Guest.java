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
	@Column(name = "email", length = 255)
//	@GeneratedValue // (currently not working)
//	@GeneratedValue (strategy = GenerationType.AUTO) // (currently not working)
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// previously (before adding IdentityColumnSupport) ...
	// @GenericGenerator(name="Event_AutoNumber_generator", strategy="increment")
	// @GeneratedValue(generator="Event_AutoNumber_generator")
	private String email;
	public String getEmail() { return email; }
	@SuppressWarnings("unused")
	private void setId(String email) { this.email = email; }

	@Column(length = 100)
	private String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	@Column(length = 100)
	private String title;
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "guests")
	private List<Event> events = new ArrayList<>();
	public List<Event> getEvents() { return this.events; }
	public void setEvents(List<Event> events) { this.events = events; }

	public Guest() { }
	
	public Guest(String email, String name) {
		this.email = email;
		this.name = name; 
		}
	
	public Guest(String email, String name, List<Event> events) {
		this.email = email;
		this.name = name;
		this.events = events;
	}
}
