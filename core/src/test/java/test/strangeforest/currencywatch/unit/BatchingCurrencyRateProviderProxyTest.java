package test.strangeforest.currencywatch.unit;

import java.util.*;

import org.junit.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;
import org.strangeforest.currencywatch.core.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static test.strangeforest.currencywatch.TestData.*;

public class BatchingCurrencyRateProviderProxyTest {

	@Test
	public void getRate() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (BatchingCurrencyRateProviderProxy batchingProvider = createBatchedProvider(provider, listener, 1)) {
			RateValue rate = batchingProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);

			assertEquals(RATE, rate);
			verify(listener).newRate(any(CurrencyRateEvent.class));
		}
	}

	@Test
	public void getRates() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(eq(BASE_CURRENCY), eq(CURRENCY), any(Collection.class))).thenAnswer(new Answer<Object>() {
			@Override public Object answer(InvocationOnMock invocation) throws Throwable {
				TreeMap<Date, RateValue> dateRates = new TreeMap<>(RATES);
				dateRates.keySet().retainAll((Collection<Date>)invocation.getArguments()[2]);
				return dateRates;
			}
		});
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		int batchSize = 2;
		try (BatchingCurrencyRateProviderProxy parallelProvider = createBatchedProvider(provider, listener, batchSize)) {
			Map<Date, RateValue> rates = parallelProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet());

			assertEquals(RATES, rates);
			int batchCount = (RATES.size()+1)/batchSize;
			verify(provider, times(batchCount)).getRates(eq(BASE_CURRENCY), eq(CURRENCY), any(Collection.class));
			verify(listener, times(batchCount)).newRates(any(CurrencyRateEvent[].class));
		}
	}

	private BatchingCurrencyRateProviderProxy createBatchedProvider(CurrencyRateProvider provider, CurrencyRateListener listener, int batchSize) {
		BatchingCurrencyRateProviderProxy batchingProvider = new BatchingCurrencyRateProviderProxy(provider, batchSize);
		batchingProvider.addListener(listener);
		batchingProvider.init();
		return batchingProvider;
	}
}
