package test.strangeforest.currencywatch.integration.jdbc;

import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.jdbc.*;
import org.strangeforest.db.*;
import org.strangeforest.db.logging.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.*;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static org.testng.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;
import static test.strangeforest.currencywatch.integration.jdbc.H2Data.*;

public class JDBCCurrencyRateProviderIT {

	private ConnectionPoolDataSource smDataSource;
	private ConnectionPoolDataSource dataSource;
	private UpdatableCurrencyRateProvider currencyRateProvider;

	@BeforeClass
	private void setUp() throws ClassNotFoundException {
		smDataSource = new ConnectionPoolDataSource(DRIVER_CLASS, ADMIN_JDBC_URL, ADMIN_USERNAME, ADMIN_PASSWORD);
		smDataSource.init();
		dataSource = new ConnectionPoolDataSource(DRIVER_CLASS, APP_JDBC_URL, APP_USERNAME, APP_PASSWORD);
		dataSource.setLogger(new DBConnectionPoolLogger(JDBCCurrencyRateProvider.class.getPackage().getName()));
		dataSource.init();
		ITUtil.deleteFiles(H2_DATA_DIR, H2_DATA_FILE_NAME + "\\..+\\.db");
		SchemaManager schemaManager = new SchemaManager(smDataSource, DIALECT);
		schemaManager.setUsername(APP_USERNAME);
		schemaManager.setPassword(APP_PASSWORD);
		currencyRateProvider = new JDBCCurrencyRateProvider(schemaManager, dataSource);
		currencyRateProvider.init();
	}

	@AfterClass
	private void cleanUp() {
		currencyRateProvider.close();
		dataSource.close();
		smDataSource.close();
	}

	@Test
	public void setRate() {
		currencyRateProvider.setRate(BASE_CURRENCY, CURRENCY, DATE, RATE);
	}

	@Test(dependsOnMethods = "setRate")
	public void getRate() {
		RateValue fetchedRate = currencyRateProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);
		assertEquals(fetchedRate.withScale(1), RATE);
	}

	@Test(dependsOnMethods = "getRate")
	public void setRates() {
		currencyRateProvider.setRates(BASE_CURRENCY, CURRENCY, RATES);
	}

	@Test(dependsOnMethods = "setRates")
	public void getRates() {
		final Map<Date, RateValue> fetchedRates = currencyRateProvider.getRates(BASE_CURRENCY, CURRENCY, DATES);
		assertEquals(fetchedRates.keySet().stream().collect(toMap(identity(), date -> fetchedRates.get(date).withScale(1))), RATES);
	}
}
