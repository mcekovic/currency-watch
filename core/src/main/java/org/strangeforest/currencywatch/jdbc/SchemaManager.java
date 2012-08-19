package org.strangeforest.currencywatch.jdbc;

import javax.sql.*;

import com.finsoft.db.gateway.*;

public class SchemaManager {

	private final DBGateway db;

	private static final int SCHEMA_VERSION = 1;

	public SchemaManager(DataSource dataSource, String dialect) {
		super();
		db = new DBGateway(dataSource, SQLsFactory.getSQLs(getClass(), dialect));
	}


	public void ensureSchema() {
		if (schemaExists()) {
			int version = getSchemaVersion();
			if (version < SCHEMA_VERSION)
				upgradeSchema(version, SCHEMA_VERSION);
		}
		else
			createSchema();
	}

	private boolean schemaExists() {
		return db.fetchScalar("SchemaExists") != null;
	}

	private int getSchemaVersion() {
		Number version = db.fetchScalar("FetchSchemaVersion");
		return version.intValue();
	}

	private void createSchema() {
		db.executeDDL("CreateUser");
		db.executeDDL("CreateSchema");
		db.executeDDL("CreateSchemaVersionTable");
		db.executeDDL("CreateCurrencyRateTable");
		db.executeDDL("CreateCurrencyRatePK");
		db.executeDDL("CreateCurrencyRateDateIndex");
	}

	private void upgradeSchema(int oldVersion, int newVersion) {
	}
}
