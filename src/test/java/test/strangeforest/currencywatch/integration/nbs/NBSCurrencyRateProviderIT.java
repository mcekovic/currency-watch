package test.strangeforest.currencywatch.integration.nbs;

import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.nbs.*;
import org.testng.annotations.*;

public class NBSCurrencyRateProviderIT {

	private CurrencyRateProvider currencyRateProvider;

	@BeforeClass
	public void setUp() throws CurrencyRateException {
		currencyRateProvider = new NBSCurrencyRateProvider();
		currencyRateProvider.init();
	}

	@AfterClass
	public void cleanUp() {
		currencyRateProvider.close();
	}

	@Test
	public void getRateTest() throws CurrencyRateException {
		RateValue rate = currencyRateProvider.getRate("DIN", "EUR", new GregorianCalendar(2006, 11, 6).getTime());
		System.out.println(rate);
	}
}