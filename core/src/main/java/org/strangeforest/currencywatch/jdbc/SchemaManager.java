package org.strangeforest.currencywatch.jdbc;

import javax.sql.*;

import org.slf4j.*;
import org.strangeforest.db.*;
import org.strangeforest.db.gateway.*;

import static org.strangeforest.db.gateway.DataHelper.*;

public class SchemaManager {

	private final String dialect;
	private final int schemaVersion;
	private final DBGateway db;
	private String username = USERNAME;
	private String password = PASSWORD;

	public static final int VERSION = 2;
	private static final String USERNAME = "CW";
	private static final String PASSWORD = "CW";

	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaManager.class);

	public SchemaManager(DataSource dataSource, String dialect) {
		this(dataSource, dialect, VERSION);
	}

	public SchemaManager(DataSource dataSource, String dialect, int schemaVersion) {
		super();
		this.dialect = dialect;
		this.schemaVersion = schemaVersion;
		db = new DBGateway(dataSource, SQLsFactory.getSQLs(getClass(), dialect));
	}

	public String getDialect() {
		return dialect;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
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
		LOGGER.info("Creating database schema...");
		SQLTransformer usernameTransformer = sql -> sql.replaceElement("username", username);
		db.executeDDL("CreateUser", usernameTransformer, st -> setString(st, "password", password));
		db.executeDDL("CreateSchema", usernameTransformer);
		db.executeDDL("CreateSchemaVersionTable");
		createCurrencyRateTable();
		updateSchemaVersion(schemaVersion, false);
		LOGGER.info("Database schema created.");
	}

	public void upgradeSchema(int oldVersion, int newVersion) {
		LOGGER.info(String.format("Upgrading database schema from version %1$d to version %2$d...", oldVersion, newVersion));
		dropCurrencyRateTable();
		createCurrencyRateTable();
		updateSchemaVersion(newVersion, true);
		LOGGER.info("Database schema upgraded.");
	}

	private void createCurrencyRateTable() {
		db.executeDDL("CreateCurrencyRateTable");
		db.executeDDL("CreateCurrencyRatePK");
		db.executeDDL("CreateCurrencyRateDateIndex");
	}

	private void dropCurrencyRateTable() {
		db.executeDDL("DropCurrencyRateTable");
	}

	private void updateSchemaVersion(int version, boolean upgrade) {
		db.executeUpdate("SetSchemaVersion", (PreparedStatementHelper st) -> {
			setInt(st, "version", version);
			setBoolean(st, "upgrade", upgrade);
		});
	}
}
