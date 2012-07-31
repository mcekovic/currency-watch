package test.strangeforest.currencywatch.system;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.db4o.*;

import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRateIT {

	private CurrencyRateProvider currencyRateProvider;

	private static final String DB4O_DATA_FILE = "data/test-rates.db4o";

	@BeforeClass
	public void setUp() {
		ObservableCurrencyRateProvider nbsProvider = new NBSCurrencyRateProvider();
		nbsProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				System.out.println(rateEvent);
			}
		});
		nbsProvider.init();
		Db4oCurrencyRateProviderIT.deleteDb4oDataFile();
		currencyRateProvider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider(DB4O_DATA_FILE),
			new ParallelCurrencyRateProviderProxy(nbsProvider, 5)
		);
	}

	@AfterClass
	public void cleanUp() {
		currencyRateProvider.close();
	}

	@Test
	public void getRate() {
		CurrencyRate rate = new CurrencyRate(SYMBOL_FROM, SYMBOL_TO, currencyRateProvider);
		rate.getRate(DATE);
		System.out.println(rate);
	}

	@Test
	public void getRates() {
		CurrencyRate rate = new CurrencyRate(SYMBOL_FROM, SYMBOL_TO, currencyRateProvider);
		rate.getRates(DATES);
		System.out.println(rate);
	}
}