package org.strangeforest.currencywatch.jdbc;

import java.sql.*;
import javax.sql.*;

import com.finsoft.db.*;
import com.finsoft.db.gateway.*;

import static com.finsoft.db.gateway.DataHelper.*;

public class SchemaManager {

	private final DBGateway db;
	private final int schemaVersion;

	public static final int SCHEMA_VERSION = 1;

	public SchemaManager(DataSource dataSource, String dialect) {
		this(dataSource, dialect, SCHEMA_VERSION);
	}

	public SchemaManager(DataSource dataSource, String dialect, int schemaVersion) {
		super();
		this.schemaVersion = schemaVersion;
		db = new DBGateway(dataSource, SQLsFactory.getSQLs(getClass(), dialect));
	}

	public void ensureSchema() {
		if (schemaExists()) {
			int version = getSchemaVersion();
			if (version < schemaVersion)
				upgradeSchema(version, schemaVersion);
		}
		else
			createSchema();
	}

	public boolean schemaExists() {
		return db.fetchScalar("SchemaExists") != null;
	}

	public int getSchemaVersion() {
		Number version = db.fetchScalar("FetchSchemaVersion");
		return version.intValue();
	}

	public void createSchema() {
		db.executeDDL("CreateUser");
		db.executeDDL("CreateSchema");
		db.executeDDL("CreateSchemaVersionTable");
		db.executeDDL("CreateCurrencyRateTable");
		db.executeDDL("CreateCurrencyRatePK");
		db.executeDDL("CreateCurrencyRateDateIndex");
		updateSchemaVersion(schemaVersion, false);
	}

	public void upgradeSchema(int oldVersion, int newVersion) {
		updateSchemaVersion(newVersion, true);
	}

	private void updateSchemaVersion(final int version, final boolean upgrade) {
		db.executeUpdate("SetSchemaVersion", new StatementPreparer() {
			@Override public void prepare(PreparedStatementHelper st) throws SQLException {
				setInt(st, "version", version);
				setBoolean(st, "upgrade", upgrade);
			}
		});
	}
}
