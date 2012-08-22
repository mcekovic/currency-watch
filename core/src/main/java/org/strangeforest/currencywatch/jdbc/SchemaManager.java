package org.strangeforest.currencywatch.jdbc;

import java.sql.*;
import javax.sql.*;

import org.slf4j.*;

import com.finsoft.db.*;
import com.finsoft.db.gateway.*;
import com.finsoft.xml.helpers.*;

import static com.finsoft.db.gateway.DataHelper.*;

public class SchemaManager {

	private final String dialect;
	private final int schemaVersion;
	private final DBGateway db;
	private String username = USERNAME;
	private String password = PASSWORD;

	public static final int VERSION = 2;
	private static final String USERNAME = "CW";
	private static final String PASSWORD = "";

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
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
		SQLTransformer usernameTransformer = new SQLTransformer() {
			@Override public void transform(ElementHelper sql) {
				sql.replaceElement("username", username);
			}
		};
		db.executeDDL("CreateUser", usernameTransformer, new StatementPreparer() {
			@Override public void prepare(PreparedStatementHelper st) throws SQLException {
				setString(st, "password", password);
			}
		});
		db.executeDDL("CreateSchema", usernameTransformer);
		db.executeDDL("CreateSchemaVersionTable");
		createCurrencyRateTable();
		updateSchemaVersion(schemaVersion, false);
		LOGGER.info("Database schema created.");
	}

	public void upgradeSchema(int oldVersion, int newVersion) {
		LOGGER.info(String.format("Upgrading database schema from version %1$d to version %2$d...", oldVersion, newVersion));
		db.executeDDL("DropCurrencyRateTable");
		createCurrencyRateTable();
		updateSchemaVersion(newVersion, true);
		LOGGER.info("Database schema upgraded.");
	}

	private void createCurrencyRateTable() {
		db.executeDDL("CreateCurrencyRateTable");
		db.executeDDL("CreateCurrencyRatePK");
		db.executeDDL("CreateCurrencyRateDateIndex");
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
