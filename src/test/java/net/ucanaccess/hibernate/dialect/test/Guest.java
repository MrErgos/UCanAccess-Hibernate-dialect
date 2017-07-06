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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * 
 * Guest entity - a person attending one or more Events
 * 
 * This object is the "inverse" side of the many-to-many relationship with the Event entity
 * 
 * For details on "owner side" vs. "inverse side", see https://stackoverflow.com/a/19291538/2144390
 *
 */
@Entity
public class Guest {
	@Id
	// an example of a non-identity (i.e., not AutoNumber) primary key
	@Column(name = "email", length = 255)
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

	public Guest() {
		// no-argument constructor required by Hibernate
	}
	
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
