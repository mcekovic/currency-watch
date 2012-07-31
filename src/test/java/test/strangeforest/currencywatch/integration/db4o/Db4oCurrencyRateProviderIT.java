package test.strangeforest.currencywatch.integration.db4o;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.testng.*;
import org.testng.annotations.*;

public class Db4oCurrencyRateProviderIT {

	private UpdatableCurrencyRateProvider currencyRateProvider;
	private RateValue rate;
	private Date date;

	private static final String DB4O_DATA_FILE = "data/test-rates-db4o.db4o";

	@BeforeClass
	private void setUp() {
		File file = new File(DB4O_DATA_FILE);
		file.delete();
		file.deleteOnExit();
		currencyRateProvider = new Db4oCurrencyRateProvider(DB4O_DATA_FILE);
		rate = new RateValue(81.0, 79.0, 80.0);
		date = new GregorianCalendar(2006, 11, 6).getTime();
	}

	@AfterClass
	private void cleanUp() {
		currencyRateProvider.close();
	}

	@Test
	public void setRateTest() throws CurrencyRateException {
		currencyRateProvider.setRate("DIN", "EUR", date, rate);
	}

	@Test(dependsOnMethods = "setRateTest")
	public void getRateTest() throws CurrencyRateException {
		RateValue fetchedRate = currencyRateProvider.getRate("DIN", "EUR", date);
		Assert.assertEquals(fetchedRate, rate);
	}
}