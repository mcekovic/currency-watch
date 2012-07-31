package test.strangeforest.currencywatch.system;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;
import org.testng.annotations.*;

public class CurrencyRateIT {

	private CurrencyRateProvider currencyRateProvider;

	private static final String DB4O_DATA_FILE = "data/test-rates.db4o";
	private static final Date FROM_DATE = new GregorianCalendar(2006, 11, 1).getTime();
	private static final Date TO_DATE = new GregorianCalendar(2006, 11, 6).getTime();

	@BeforeClass
	public void setUp() {
		ObservableCurrencyRateProvider nbsProvider = new NBSCurrencyRateProvider();
		nbsProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				System.out.println(rateEvent);
			}
		});
		nbsProvider.init();
		File file = new File(DB4O_DATA_FILE);
		file.delete();
		file.deleteOnExit();
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
	public void test() {
		CurrencyRate rate = new CurrencyRate("DIN", "EUR", currencyRateProvider);
		System.out.println(rate.getRates(new DateRange(FROM_DATE, TO_DATE)));
	}
}