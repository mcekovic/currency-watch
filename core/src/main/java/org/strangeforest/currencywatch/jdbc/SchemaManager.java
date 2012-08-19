package org.strangeforest.currencywatch.jdbc;

import java.io.*;

import javax.sql.*;

import com.finsoft.db.gateway.*;

public class SchemaManager {

	private final DataSource dataSource;
	private String dialect = DIALECT;
	private DBGateway db;

	private static final String DIALECT = "H2";
	private static final int SCHEMA_VERSION = 1;

	public SchemaManager(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public SQLs getSQLS(Class<?> cls) {
		String sqlsName = cls.getSimpleName();
		if (dialect != null)
			sqlsName += '.' + dialect;
		sqlsName += ".sqls";
		InputStream in = cls.getResourceAsStream(sqlsName);
		if (in != null)
			return new SQLs(in);
		else
			throw new DBException("Cannot find SQLs: " + sqlsName);
	}


	public void ensureSchema() {
		initDB();
		if (schemaExists()) {
			int version = getSchemaVersion();
			if (version < SCHEMA_VERSION)
				upgradeSchema(version, SCHEMA_VERSION);
		}
		else
			createSchema();
	}

	public void initDB() {
		db = new DBGateway(dataSource, getSQLS(getClass()));
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
		db.executeDDL("CreateCurrencyRateTable");
		db.executeDDL("CreateCurrencyRatePK");
		db.executeDDL("CreateCurrencyRateDateIndex");
	}

	private void upgradeSchema(int oldVersion, int newVersion) {
	}
}
