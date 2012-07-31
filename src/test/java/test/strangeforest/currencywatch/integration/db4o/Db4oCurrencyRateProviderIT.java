package test.strangeforest.currencywatch.integration.db4o;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.testng.*;
import org.testng.annotations.*;

import static test.strangeforest.currencywatch.TestData.*;

public class Db4oCurrencyRateProviderIT {

	private UpdatableCurrencyRateProvider currencyRateProvider;

	@BeforeClass
	private void setUp() {
		deleteDb4oDataFile();
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
		Assert.assertEquals(fetchedRate, RATE);
	}

	@Test(dependsOnMethods = "getRate")
	public void setRates() {
		currencyRateProvider.setRates(SYMBOL_FROM, SYMBOL_TO, RATES);
	}

	@Test(dependsOnMethods = "setRates")
	public void getRates() {
		Map<Date, RateValue> fetchedRates = currencyRateProvider.getRates(SYMBOL_FROM, SYMBOL_TO, DATES);
		Assert.assertEquals(fetchedRates, RATES);
	}

	public static void deleteDb4oDataFile() {
		File file = new File(DB4O_DATA_FILE);
		file.delete();
		file.deleteOnExit();
	}
}