package test.strangeforest.currencywatch.integration.db4o;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.*;

import static org.testng.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class Db4oCurrencyRateProviderIT {

	private UpdatableCurrencyRateProvider currencyRateProvider;

	private static final String DB4O_DATA_FILE = "data/test-rates-db4o.db4o";
	private static final String DB4O_DATA_FILE_UPGRADE = "data/upgrade-rates-db4o.db4o";

	@BeforeClass
	private void setUp() {
		ITUtil.deleteFile(DB4O_DATA_FILE);
		ITUtil.deleteFile(DB4O_DATA_FILE_UPGRADE);
		currencyRateProvider = new Db4oCurrencyRateProvider(DB4O_DATA_FILE);
	}

	@AfterClass
	private void cleanUp() {
		currencyRateProvider.close();
	}

	@Test
	public void setRate() {
		currencyRateProvider.setRate(SYMBOL_FROM, SYMBOL_TO, DATE, RATE);
	}

	@Test(dependsOnMethods = "setRate")
	public void getRate() {
		RateValue fetchedRate = currencyRateProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE);
		assertEquals(fetchedRate, RATE);
	}

	@Test(dependsOnMethods = "getRate")
	public void setRates() {
		currencyRateProvider.setRates(SYMBOL_FROM, SYMBOL_TO, RATES);
	}

	@Test(dependsOnMethods = "setRates")
	public void getRates() {
		Map<Date, RateValue> fetchedRates = currencyRateProvider.getRates(SYMBOL_FROM, SYMBOL_TO, DATES);
		assertEquals(fetchedRates, RATES);
	}

	@Test(dependsOnMethods = "getRates")
	public void upgradeData() throws NoSuchFieldException, IllegalAccessException {
		new Db4oCurrencyRateProvider(DB4O_DATA_FILE_UPGRADE).close();
		assertTrue(new File(DB4O_DATA_FILE_UPGRADE).exists());

		new Db4oCurrencyRateProvider(DB4O_DATA_FILE_UPGRADE, Db4oCurrencyRateProvider.CURRENT_VERSION + 1).close();
		assertTrue(new File(DB4O_DATA_FILE_UPGRADE).exists());
	}
}