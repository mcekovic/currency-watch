package test.strangeforest.currencywatch.unit.core;

import java.util.*;

import org.junit.*;
import org.mockito.*;
import org.strangeforest.currencywatch.core.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.*;
import static test.strangeforest.currencywatch.TestData.*;

public class ParallelCurrencyRateProviderProxyTest {

	@Test
	public void getRate() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider parallelProvider = createParallelProvider(provider, listener, 1)) {
			RateValue rate = parallelProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);

			assertThat(rate).isEqualTo(RATE);
			verify(listener).newRate(any(CurrencyRateEvent.class));
		}
	}

	@Test
	public void getRates() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(eq(BASE_CURRENCY), eq(CURRENCY), any(Date.class))).thenAnswer(invocation -> RATES.get(invocation.getArgumentAt(2, Date.class)));
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider parallelProvider = createParallelProvider(provider, listener, 2)) {
			Map<Date, RateValue> rates = parallelProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet());

			assertThat(rates).isEqualTo(RATES);
			verify(listener, times(RATES.size())).newRate(any(CurrencyRateEvent.class));
		}
	}

	@Test
	public void getRatesRetried() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenThrow(new CurrencyRateException("Booom!")).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider parallelProvider = createParallelProvider(provider, listener, 1)) {
			Map<Date, RateValue> rates = parallelProvider.getRates(BASE_CURRENCY, CURRENCY, Collections.singleton(DATE));

			assertThat(rates).containsExactly(entry(DATE, RATE));
			verify(provider, times(2)).getRate(BASE_CURRENCY, CURRENCY, DATE);
			verify(listener).newRate(any(CurrencyRateEvent.class));
			verify(listener).error(any(String.class));
		}
	}

	@Test
	public void getRatesRetryFail() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenThrow(new CurrencyRateException("Booom!"));
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		int retryCount = 2;
		try (ParallelCurrencyRateProviderProxy parallelProvider = createParallelProvider(provider, listener, 1)) {
			parallelProvider.setRetryCount(retryCount);
			parallelProvider.setMaxExceptions(0);
			try {
				parallelProvider.getRates(BASE_CURRENCY, CURRENCY, Collections.singleton(DATE));
				failBecauseExceptionWasNotThrown(CurrencyRateException.class);
			}
			catch (CurrencyRateException ex) {
				InOrder inOrder = inOrder(provider);
				inOrder.verify(provider).init();
				inOrder.verify(provider, times(1+retryCount)).getRate(BASE_CURRENCY, CURRENCY, DATE);
				inOrder.verify(provider).close();
				inOrder.verify(provider).init();
				verify(listener, never()).newRate(any(CurrencyRateEvent.class));
				verify(listener, times(1+retryCount)).error(any(String.class));
			}
		}
	}

	private ParallelCurrencyRateProviderProxy createParallelProvider(CurrencyRateProvider provider, CurrencyRateListener listener, int threadCount) {
		ParallelCurrencyRateProviderProxy parallelProvider = new ParallelCurrencyRateProviderProxy(provider, threadCount);
		parallelProvider.addListener(listener);
		parallelProvider.init();
		return parallelProvider;
	}
}
