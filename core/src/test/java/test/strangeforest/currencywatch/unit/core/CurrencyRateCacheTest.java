package test.strangeforest.currencywatch.unit.core;

import org.junit.*;
import org.strangeforest.currencywatch.core.*;

import static org.junit.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRateCacheTest {

	@Test
	public void cachedValuesAreRetrieved() {
		CurrencyRateCache cache = new CurrencyRateCache();
		cache.setRate(BASE_CURRENCY, CURRENCY, DATE, RATE);

		RateValue cachedRate = cache.getRate(BASE_CURRENCY, CURRENCY, DATE);
		assertEquals(RATE, cachedRate);
	}

	@Test
	public void missingValuesReturnNull() {
		CurrencyRateCache cache = new CurrencyRateCache();
		cache.setRate(BASE_CURRENCY, CURRENCY, DATE1, RATE);

		RateValue nullRate = cache.getRate(BASE_CURRENCY, CURRENCY, DATE2);
		assertNull(nullRate);
	}
}
