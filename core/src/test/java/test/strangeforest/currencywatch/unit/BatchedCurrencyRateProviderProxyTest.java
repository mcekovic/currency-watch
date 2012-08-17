package test.strangeforest.currencywatch.unit;

import java.util.*;

import org.junit.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;
import org.strangeforest.currencywatch.core.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static test.strangeforest.currencywatch.TestData.*;

public class BatchedCurrencyRateProviderProxyTest {

	@Test
	public void getRate() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (BatchedCurrencyRateProviderProxy batchedProvider = createBatchedProvider(provider, listener, 1)) {
			RateValue rate = batchedProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);

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
		try (BatchedCurrencyRateProviderProxy parallelProvider = createBatchedProvider(provider, listener, batchSize)) {
			Map<Date, RateValue> rates = parallelProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet());

			assertEquals(RATES, rates);
			int batchCount = (RATES.size()+1)/batchSize;
			verify(provider, times(batchCount)).getRates(eq(BASE_CURRENCY), eq(CURRENCY), any(Collection.class));
			verify(listener, times(batchCount)).newRates(any(CurrencyRateEvent[].class));
		}
	}

	private BatchedCurrencyRateProviderProxy createBatchedProvider(CurrencyRateProvider provider, CurrencyRateListener listener, int batchSize) {
		BatchedCurrencyRateProviderProxy batchedProvider = new BatchedCurrencyRateProviderProxy(provider, batchSize);
		batchedProvider.addListener(listener);
		batchedProvider.init();
		return batchedProvider;
	}
}
