package test.strangeforest.currencywatch.integration.rest;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.rest.*;
import org.testng.annotations.*;

import static org.assertj.core.api.Assertions.*;
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
		assertThat(provider.ping()).isTrue();
	}

	@Test
	public void getRate() {
		RateValue rate = provider.getRate(BASE_CURRENCY, CURRENCY, DATE);
		assertThat(rate).isEqualTo(RATE);
	}

	@Test
	public void getRates() {
		Map<Date, RateValue> rates = provider.getRates(BASE_CURRENCY, CURRENCY, DATES);
		assertThat(rates).isEqualTo(RATES);
	}

	@Test
	public void unknownDate() {
		RateValue rate = provider.getRate(BASE_CURRENCY, CURRENCY, UNKNOWN_DATE);
		assertThat(rate).isNull();
	}
}
