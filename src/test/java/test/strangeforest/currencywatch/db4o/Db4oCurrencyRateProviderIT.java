package test.strangeforest.currencywatch.db4o;

import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.testng.*;
import org.testng.annotations.*;

public class Db4oCurrencyRateProviderIT {

	@Test
	public void setAndGetRateTest() throws CurrencyRateException {
		Db4oCurrencyRateProvider provider = new Db4oCurrencyRateProvider("data/test-rates.db4o");
		Date date = new GregorianCalendar(2006, 11, 6).getTime();
		RateValue rate = new RateValue(81.0, 79.0, 80.0);
		provider.setRate("DIN", "EUR", date, rate);
		RateValue fetchedRate = provider.getRate("DIN", "EUR", date);
		System.out.println(fetchedRate);
		Assert.assertEquals(fetchedRate, rate);
	}
}