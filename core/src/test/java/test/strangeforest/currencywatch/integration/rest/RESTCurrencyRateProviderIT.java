package test.strangeforest.currencywatch.integration.rest;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.rest.*;
import org.testng.annotations.*;

import static org.testng.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class RESTCurrencyRateProviderIT extends CurrencyRateResourceFixture{

	private RESTCurrencyRateProvider provider;

	@BeforeClass
	public void setUp() throws IOException {
		provider = new RESTCurrencyRateProvider(URI);
		provider.init();
	}

	@AfterClass
	public void cleanUp() {
		provider.close();
	}

	@Test
	public void ping() {
		assertTrue(provider.ping());
	}

	@Test
	public void getRate() {
		RateValue rate = provider.getRate(BASE_CURRENCY, CURRENCY, DATE);
		assertEquals(rate, RATE);
	}

	@Test
	public void getRates() {
		Map<Date, RateValue> rates = provider.getRates(BASE_CURRENCY, CURRENCY, DATES);
		assertEquals(rates, RATES);
	}

	@Test
	public void unknownDate() {
		RateValue rate = provider.getRate(BASE_CURRENCY, CURRENCY, UNKNOWN_DATE);
		assertEquals(rate, null);
	}
}
