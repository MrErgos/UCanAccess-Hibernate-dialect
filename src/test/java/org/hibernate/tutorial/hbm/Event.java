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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "EVENTS")
public class Event {
	@Id
	@Column(name = "EVENT_ID")
//	@GeneratedValue // (currently not working)
//	@GeneratedValue (strategy = GenerationType.AUTO) // (currently not working)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// previously (before adding IdentityColumnSupport) ...
	// @GenericGenerator(name="Event_AutoNumber_generator", strategy="increment")
	// @GeneratedValue(generator="Event_AutoNumber_generator")
	private Integer id;

	public Integer getId() {
		return id;
	}

	@SuppressWarnings("unused")
	private void setId(Integer id) {
		this.id = id;
	}

	// Access databases *often* have spaces in column names
	// ... see "globally_quoted_identifiers" property in hibernate.cfg.xml
	@Column(name = "EVENT DATE")
	private Date date;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	// limited-length String -> VARCHAR(100) [via Hibernate] -> TEXT(100) [via UCanAccess]
	@Column(length = 100)
	private String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	// unspecified-length String -> VARCHAR(255) [via Hibernate] -> TEXT(255) [via UCanAccess]
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// @Lob: unlimited-length String -> CLOB (via Hibernate) -> MEMO (via UCanAccess)
	@Lob
	private String comments;

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	// Currency (actually mapped to DECIMAL in Access)
	@Column(precision = 19, scale = 4) // required, otherwise defaults to (19,2)
	private BigDecimal fee;

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public Event() {
		// this form used by Hibernate
	}

	public Event(String title, Date date) {
		// for application use, to create new events
		this.title = title;
		this.date = date;
	}

}
