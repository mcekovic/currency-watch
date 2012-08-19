package test.strangeforest.currencywatch.integration.jdbc;

import java.util.*;
import javax.sql.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.jdbc.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.*;

import com.finsoft.db.*;
import com.finsoft.util.*;

import static org.testng.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class JDBCCurrencyRateProviderIT {

	private UpdatableCurrencyRateProvider currencyRateProvider;

	private static final String DRIVER_CLASS = "org.h2.Driver";
	private static final String H2_DATA_DIR = "data";
	private static final String H2_DATA_FILE_NAME = "test-rates-h2";
	private static final String H2_DATA_FILE = H2_DATA_DIR + '/' + H2_DATA_FILE_NAME;
	private static final String JDBC_URL = "jdbc:h2:" + H2_DATA_FILE;
	private static final String USERNAME = "sa";
	private static final String PASSWORD = "";
	private static final String DIALECT = "H2";

	@BeforeClass
	private void setUp() throws ClassNotFoundException {
		DataSource dataSource = new DriverDataSource(DRIVER_CLASS, JDBC_URL, USERNAME, PASSWORD);
		ITUtil.deleteFiles(H2_DATA_DIR, H2_DATA_FILE_NAME + "\\..+\\.db");
		currencyRateProvider = new JDBCCurrencyRateProvider(dataSource, DIALECT);
		currencyRateProvider.init();
	}

	@AfterClass
	private void cleanUp() {
		currencyRateProvider.close();
	}

	@Test
	public void setRate() {
		currencyRateProvider.setRate(BASE_CURRENCY, CURRENCY, DATE, RATE);
	}

	@Test(dependsOnMethods = "setRate")
	public void getRate() {
		RateValue fetchedRate = currencyRateProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);
		assertEquals(fetchedRate.setScale(1), RATE);
	}

	@Test(dependsOnMethods = "getRate")
	public void setRates() {
		currencyRateProvider.setRates(BASE_CURRENCY, CURRENCY, RATES);
	}

	@Test(dependsOnMethods = "setRates")
	public void getRates() {
		final Map<Date, RateValue> fetchedRates = currencyRateProvider.getRates(BASE_CURRENCY, CURRENCY, DATES);
		assertEquals(Algorithms.transformToMap(fetchedRates.keySet(), new Transformer<Date, RateValue>() {
			@Override public RateValue transform(Date date) {
				return fetchedRates.get(date).setScale(1);
			}
		}), RATES);
	}
}
