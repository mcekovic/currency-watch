package test.strangeforest.currencywatch.system;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;
import org.testng.annotations.*;

public class CurrencyRateIT {

	private CurrencyRateProvider currencyRateProvider;
	private Date fromDate;
	private Date toDate;

	private static final String DB4O_DATA_FILE = "data/test-rates.db4o";

	@BeforeClass
	public void setUp() throws CurrencyRateException {
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
		fromDate = new GregorianCalendar(2006, 11, 1).getTime();
		toDate = new GregorianCalendar(2006, 11, 6).getTime();
	}

	@AfterClass
	public void cleanUp() {
		currencyRateProvider.dispose();
	}

	@Test
	public void test() throws CurrencyRateException {
		CurrencyRate rate = new CurrencyRate("DIN", "EUR", currencyRateProvider);
		System.out.println(rate.getRates(new DateRange(fromDate, toDate)));
	}
}