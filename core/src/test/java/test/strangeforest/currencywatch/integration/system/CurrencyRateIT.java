package test.strangeforest.currencywatch.integration.system;

import java.io.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.mapdb.*;
import org.strangeforest.currencywatch.nbs.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.*;

import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRateIT {

	private CurrencyRateProvider currencyRateProvider;

	private static final String MAPDB_PATH_NAME = "target/data/test-rates-system-db";

	@BeforeClass
	public void setUp() throws IOException {
		ITUtil.ensurePath(MAPDB_PATH_NAME);
		ITUtil.deleteFile(MAPDB_PATH_NAME);
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider(new CurrencyRateTracer());
		currencyRateProvider = new ChainedCurrencyRateProvider(
			new MapDBCurrencyRateProvider(MAPDB_PATH_NAME),
			new ParallelCurrencyRateProviderProxy(remoteProvider, 5)
		);
		currencyRateProvider.init();
	}

	@AfterClass
	public void cleanUp() {
		currencyRateProvider.close();
	}

	@Test
	public void getRate() {
		CurrencyRates rates = new CurrencyRates(BASE_CURRENCY, CURRENCY, currencyRateProvider);
		rates.getRate(DATE);
		System.out.println(rates);
	}

	@Test
	public void getRates() {
		CurrencyRates rates = new CurrencyRates(BASE_CURRENCY, CURRENCY, currencyRateProvider);
		rates.getRates(DATES);
		System.out.println(rates);
	}
}