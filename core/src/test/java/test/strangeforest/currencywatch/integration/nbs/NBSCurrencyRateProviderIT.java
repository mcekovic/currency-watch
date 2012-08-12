package test.strangeforest.currencywatch.integration.nbs;

import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.nbs.*;
import org.testng.annotations.*;

import static test.strangeforest.currencywatch.TestData.*;

public class NBSCurrencyRateProviderIT {

	private CurrencyRateProvider currencyRateProvider;

	@BeforeClass
	public void setUp() {
		currencyRateProvider = new NBSCurrencyRateProvider();
		currencyRateProvider.init();
	}

	@AfterClass
	public void cleanUp() {
		currencyRateProvider.close();
	}

	@Test
	public void getRate() {
		RateValue rate = currencyRateProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);
		System.out.println(rate);
	}

	@Test
	public void getRates() {
		Map<Date, RateValue> rates = currencyRateProvider.getRates(BASE_CURRENCY, CURRENCY, Arrays.asList(DATE2, DATE3));
		System.out.println(rates);
	}
}