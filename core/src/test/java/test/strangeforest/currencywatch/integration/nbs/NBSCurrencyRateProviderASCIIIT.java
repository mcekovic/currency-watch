package test.strangeforest.currencywatch.integration.nbs;

import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.nbs.*;
import org.testng.annotations.*;

import static test.strangeforest.currencywatch.TestData.*;

public class NBSCurrencyRateProviderASCIIIT {

	private NBSCurrencyRateProvider currencyRateProvider;

	@BeforeClass
	public void setUp() {
		currencyRateProvider = new NBSCurrencyRateProvider();
		currencyRateProvider.setFormat(NBSFormat.ASCII);
		currencyRateProvider.init();
	}

	@AfterClass
	public void cleanUp() {
		currencyRateProvider.close();
	}

	@Test
	public void getRates() {
		Map<Date, RateValue> rates = currencyRateProvider.getRates(BASE_CURRENCY, CURRENCY, Arrays.asList(DATE5));
		System.out.println(rates);
	}
}