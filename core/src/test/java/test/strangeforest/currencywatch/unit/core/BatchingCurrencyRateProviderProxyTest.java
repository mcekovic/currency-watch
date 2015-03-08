package test.strangeforest.currencywatch.unit.core;

import java.util.*;

import org.junit.*;
import org.mockito.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;
import org.strangeforest.currencywatch.core.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static test.strangeforest.currencywatch.TestData.*;

public class BatchingCurrencyRateProviderProxyTest {

	@Test
	public void getRate() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider batchingProvider = createBatchingProvider(provider, listener, 1)) {
			RateValue rate = batchingProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);

			assertThat(rate).isEqualTo(RATE);
			verify(listener).newRate(any(CurrencyRateEvent.class));
		}
	}

	@Test
	public void getRatesInBatches() {
		getRates(2);
	}

	@Test
	public void getRatesInOneBatch() {
		getRates(10);
	}

	private void getRates(int batchSize) {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(eq(BASE_CURRENCY), eq(CURRENCY), any(Collection.class))).thenAnswer(new MockProviderGetRatesAnswer());
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider parallelProvider = createBatchingProvider(provider, listener, batchSize)) {
			Map<Date, RateValue> rates = parallelProvider.getRates(BASE_CURRENCY, CURRENCY, DATES);

			assertThat(rates).isEqualTo(RATES);
			int batchCount = 1+(RATES.size()-1)/batchSize;
			verify(provider, times(batchCount)).getRates(eq(BASE_CURRENCY), eq(CURRENCY), any(Collection.class));
			verify(listener, times(batchCount)).newRates(any(CurrencyRateEvent[].class));
		}
	}


	@Test
	public void getRatesRetried() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(eq(BASE_CURRENCY), eq(CURRENCY), any(Collection.class))).thenThrow(new CurrencyRateException("Booom!")).thenAnswer(new MockProviderGetRatesAnswer());
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		int batchSize = 2;
		try (CurrencyRateProvider batchingProvider = createBatchingProvider(provider, listener, batchSize)) {
			Map<Date, RateValue> rates = batchingProvider.getRates(BASE_CURRENCY, CURRENCY, DATES);

			assertThat(rates).isEqualTo(RATES);
			int batchCount = 1+(RATES.size()-1)/batchSize;
			verify(provider, times(1+batchCount)).getRates(eq(BASE_CURRENCY), eq(CURRENCY), any(Collection.class));
			verify(listener, times(batchCount)).newRates(any(CurrencyRateEvent[].class));
			verify(listener).error(any(String.class));
		}
	}

	@Test
	public void getRatesRetryFail() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(eq(BASE_CURRENCY), eq(CURRENCY), any(Collection.class))).thenThrow(new CurrencyRateException("Booom!"));
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		int batchSize = 2;
		int retryCount = 2;
		try (BatchingCurrencyRateProviderProxy batchingProvider = createBatchingProvider(provider, listener, batchSize)) {
			batchingProvider.setRetryCount(retryCount);
			try {
				batchingProvider.getRates(BASE_CURRENCY, CURRENCY, DATES);
				failBecauseExceptionWasNotThrown(CurrencyRateException.class);
			}
			catch (CurrencyRateException ex) {
				InOrder inOrder = inOrder(provider);
				inOrder.verify(provider).init();
				inOrder.verify(provider, times(retryCount+1)).getRates(eq(BASE_CURRENCY), eq(CURRENCY), any(Collection.class));
				verify(listener, never()).newRate(any(CurrencyRateEvent.class));
				verify(listener, times(retryCount+1)).error(any(String.class));
			}
		}
	}

	private BatchingCurrencyRateProviderProxy createBatchingProvider(CurrencyRateProvider provider, CurrencyRateListener listener, int batchSize) {
		BatchingCurrencyRateProviderProxy batchingProvider = new BatchingCurrencyRateProviderProxy(provider, batchSize);
		batchingProvider.addListener(listener);
		batchingProvider.init();
		return batchingProvider;
	}

	private static class MockProviderGetRatesAnswer implements Answer<Map<Date, RateValue>> {
		@Override public Map<Date, RateValue> answer(InvocationOnMock invocation) throws Throwable {
			Map<Date, RateValue> dateRates = new TreeMap<>(RATES);
			dateRates.keySet().retainAll((Collection<Date>)invocation.getArguments()[2]);
			return dateRates;
		}
	}
}
