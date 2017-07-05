package net.ucanaccess.hibernate.dialect;

import org.hibernate.boot.Metadata;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

public class UCanAccessDialectUniqueDelegate implements UniqueDelegate {

	@Override
	public String getColumnDefinitionUniquenessFragment(Column column) {
		return " UNIQUE";
	}

	@Override
	public String getTableCreationUniqueConstraintsFragment(Table table) {
		return "";
	}

	@Override
	public String getAlterTableToAddUniqueKeyCommand(UniqueKey uniqueKey, Metadata metadata) {
		return null;
	}

	@Override
	public String getAlterTableToDropUniqueKeyCommand(UniqueKey uniqueKey, Metadata metadata) {
		return null;
	}

}
