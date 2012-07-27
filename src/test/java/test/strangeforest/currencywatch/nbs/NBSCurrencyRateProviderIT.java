package test.strangeforest.currencywatch.nbs;

import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.nbs.*;
import org.testng.annotations.*;

public class NBSCurrencyRateProviderIT {

	@Test
	public void getRateTest() throws CurrencyRateException {
		CurrencyRateProvider provider = new NBSCurrencyRateProvider();
		RateValue rate = provider.getRate("DIN", "EUR", new GregorianCalendar(2006, 11, 6).getTime());
		System.out.println(rate);
	}
}