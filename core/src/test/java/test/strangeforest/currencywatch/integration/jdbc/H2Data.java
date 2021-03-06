package test.strangeforest.currencywatch.integration.jdbc;

public abstract class H2Data {

	public static final String DRIVER_CLASS = "org.h2.Driver";
	public static final String H2_DATA_DIR = "./target/data";
	public static final String H2_DATA_FILE_NAME = "test-rates-h2";
	public static final String H2_DATA_FILE = H2_DATA_DIR + '/' + H2_DATA_FILE_NAME;
	public static final String ADMIN_JDBC_URL = "jdbc:h2:" + H2_DATA_FILE;
	public static final String APP_JDBC_URL = ADMIN_JDBC_URL + ";SCHEMA=CURRENCY_WATCH";
	public static final String ADMIN_USERNAME = "SA";
	public static final String ADMIN_PASSWORD = "SA";
	public static final String APP_USERNAME = "CW";
	public static final String APP_PASSWORD = "CW";
	public static final String DIALECT = "H2";
}
