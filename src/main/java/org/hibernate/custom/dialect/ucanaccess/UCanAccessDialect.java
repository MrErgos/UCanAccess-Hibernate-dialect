/*
   Copyright 2017 Gordon D. Thompson

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.hibernate.custom.dialect.ucanaccess;

import java.sql.Types;

import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.identity.IdentityColumnSupport;

public class UCanAccessDialect extends SQLServerDialect {
	public UCanAccessDialect() {
		super();
		registerColumnType(Types.INTEGER, "LONG");

		registerFunction("current_date", new StandardSQLFunction("Date"));
		registerFunction("current_time", new StandardSQLFunction("Time"));
		registerFunction("current_timestamp", new StandardSQLFunction("Now"));
	}

	// this is apparently how it was done in older versions of Hibernate
	//
	// public boolean supportsIdentityColumns() {
	// return true;
	// }
	//
	// public boolean hasDataTypeInIdentityColumn() {
	// return false;
	// }
	//
	// public String getIdentityColumnString() {
	// return "COUNTER";
	// }

	private static final UCanAccessDialectIdentityColumnSupport IDENTITY_COLUMN_SUPPORT = new UCanAccessDialectIdentityColumnSupport();

	@Override
	public IdentityColumnSupport getIdentityColumnSupport() {
		return (IdentityColumnSupport) IDENTITY_COLUMN_SUPPORT;
	}
}
