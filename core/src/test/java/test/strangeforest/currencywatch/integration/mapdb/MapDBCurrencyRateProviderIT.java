package test.strangeforest.currencywatch.integration.mapdb;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.mapdb.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.*;

import static org.testng.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class MapDBCurrencyRateProviderIT {

	private UpdatableCurrencyRateProvider currencyRateProvider;

	private static final String MAPDB_PATH_NAME = "target/data/test-rates-db";
	private static final String MAPDB_PATH_NAME_UPGRADE = "target/data/upgrade-rates-db";

	@BeforeClass
	private void setUp() throws IOException {
		ITUtil.ensurePath(MAPDB_PATH_NAME);
		ITUtil.deleteFile(MAPDB_PATH_NAME);
		ITUtil.deleteFile(MAPDB_PATH_NAME_UPGRADE);
		currencyRateProvider = new MapDBCurrencyRateProvider(MAPDB_PATH_NAME);
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
		assertEquals(fetchedRate, RATE);
	}

	@Test(dependsOnMethods = "getRate")
	public void setRates() {
		currencyRateProvider.setRates(BASE_CURRENCY, CURRENCY, RATES);
	}

	@Test(dependsOnMethods = "setRates")
	public void getRates() {
		Map<Date, RateValue> fetchedRates = currencyRateProvider.getRates(BASE_CURRENCY, CURRENCY, DATES);
		assertEquals(fetchedRates, RATES);
	}

	@Test(dependsOnMethods = "getRates")
	public void upgradeData() {
		try (CurrencyRateProvider provider = new MapDBCurrencyRateProvider(MAPDB_PATH_NAME_UPGRADE)) {
			provider.init();
		}
		assertTrue(new File(MAPDB_PATH_NAME_UPGRADE).exists());

		try (CurrencyRateProvider provider = new MapDBCurrencyRateProvider(MAPDB_PATH_NAME_UPGRADE, MapDBCurrencyRateProvider.CURRENT_VERSION + 1)) {
			provider.init();
		}
		assertTrue(new File(MAPDB_PATH_NAME_UPGRADE).exists());
	}
}