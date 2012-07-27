package test.strangeforest.currencywatch.core;

import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;
import org.testng.annotations.*;

public class CurrencyRateIT {

	@Test
	public void test() throws CurrencyRateException {
		ObservableCurrencyRateProvider nbsProvider = new NBSCurrencyRateProvider();
		nbsProvider.addListener(new CurrencyRateListener() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				System.out.println(rateEvent);
			}
		});
		CurrencyRateProvider provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider("data/test-rates.db4o"),
			new ParallelCurrencyRateProviderProxy(nbsProvider, 5)
		);
		CurrencyRate rate = new CurrencyRate("DIN", "EUR", provider);
		Date fromDate = new GregorianCalendar(2006, 11, 1).getTime();
		Date toDate = new GregorianCalendar(2006, 11, 6).getTime();
		System.out.println(rate.getRates(new DateRange(fromDate, toDate)));
	}
}