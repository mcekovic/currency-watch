package test.strangeforest.currencywatch.unit.core;

import java.util.*;

import org.hamcrest.*;
import org.hamcrest.Matchers;
import org.junit.*;
import org.mockito.*;
import org.strangeforest.currencywatch.core.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static test.strangeforest.currencywatch.TestData.*;

public class ChainedCurrencyRateProviderTest {

	@Test
	public void getRateFromLocalProvider() {
		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		when(localProvider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			RateValue rate = chainedProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);

			assertEquals(RATE, rate);
			verify(remoteProvider, never()).getRate(anyString(), anyString(), any(Date.class));
			verify(listener).newRate(any(CurrencyRateEvent.class));
		}
	}

	@Test
	public void getRateFromRemoteProvider() {
		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		when(localProvider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(null);
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		when(remoteProvider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			RateValue rate = chainedProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);

			assertEquals(RATE, rate);
			verify(localProvider).setRate(eq(BASE_CURRENCY), eq(CURRENCY), eq(DATE), eq(RATE));
			verify(listener).newRate(any(CurrencyRateEvent.class));
		}
	}

	@Test
	public void getAllRatesFromLocalProvider() {
		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		when(localProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet())).thenReturn(RATES);
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			Map<Date, RateValue> rates = chainedProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet());

			assertEquals(RATES, rates);
			verify(remoteProvider, never()).getRate(anyString(), anyString(), any(Date.class));
			verify(remoteProvider, never()).getRates(anyString(), anyString(), any(Collection.class));
			verify(listener).newRates(argThat(matchesEventCount(RATES.size())));
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getAllRatesFromRemoteProvider() {
		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		when(localProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet())).thenReturn(new HashMap<Date, RateValue>());
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		when(remoteProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet())).thenReturn(RATES);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			Map<Date, RateValue> rates = chainedProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet());

			assertEquals(RATES, rates);
			verify(listener).newRates(argThat(matchesEventCount(RATES.size())));
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getRatesFromBothProviders() {
		NavigableMap<Date, RateValue> localRates = RATES.subMap(DATE1, true, DATE3, true);
		NavigableMap<Date, RateValue> remoteRates = RATES.subMap(DATE4, true, DATE5, true);

		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		when(localProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet())).thenReturn(localRates);
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		when(remoteProvider.getRates(BASE_CURRENCY, CURRENCY, remoteRates.keySet())).thenReturn(remoteRates);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			Map<Date, RateValue> rates = chainedProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet());

			assertEquals(RATES, rates);
			verify(remoteProvider, never()).getRate(anyString(), anyString(), any(Date.class));
			InOrder inOrder = inOrder(listener);
	//		inOrder.verify(listener).newRates(argThat(matchesEventCount(3)));
	//		inOrder.verify(listener).newRates(argThat(matchesEventCount(2)));
			inOrder.verify(listener, times(2)).newRates(any(CurrencyRateEvent[].class)); // Mockito/Hamcrest 1.3 incompatibility workaround
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void ratesFromObservableRemoteProviderArePropagated() {
		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		when(remoteProvider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		when(remoteProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet())).thenReturn(RATES);
		ObservableCurrencyRateProviderProxy observableRemoteProvider = new ObservableCurrencyRateProviderProxy(remoteProvider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRateProvider chainedProvider = createChainedProvider(localProvider, observableRemoteProvider, listener)) {
			RateValue rate = observableRemoteProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);
			Map<Date, RateValue> rates = observableRemoteProvider.getRates(BASE_CURRENCY, CURRENCY, RATES.keySet());

			assertEquals(RATE, rate);
			assertEquals(RATES, rates);
			verify(listener).newRate(any(CurrencyRateEvent.class));
			verify(listener).newRates(argThat(matchesEventCount(RATES.size())));
		}
	}

	private ChainedCurrencyRateProvider createChainedProvider(UpdatableCurrencyRateProvider localProvider, CurrencyRateProvider remoteProvider, CurrencyRateListener listener) {
		ChainedCurrencyRateProvider chainedProvider = new ChainedCurrencyRateProvider(localProvider, remoteProvider);
		chainedProvider.addListener(listener);
		chainedProvider.init();
		return chainedProvider;
	}

	private static Matcher<CurrencyRateEvent[]> matchesEventCount(int count) {
		return Matchers.arrayWithSize(count);
	}
}
