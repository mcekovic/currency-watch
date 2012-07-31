package test.strangeforest.currencywatch.unit;

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
		when(localProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(RATE);
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (ChainedCurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			RateValue rate = chainedProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE);

			assertEquals(RATE, rate);
			verify(remoteProvider, never()).getRate(anyString(), anyString(), any(Date.class));
			verify(listener).newRate(any(CurrencyRateEvent.class));
		}
	}

	@Test
	public void getRateFromRemoteProvider() {
		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		when(localProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(null);
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		when(remoteProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (ChainedCurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			RateValue rate = chainedProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE);

			assertEquals(RATE, rate);
			verify(localProvider).setRate(eq(SYMBOL_FROM), eq(SYMBOL_TO), eq(DATE), eq(RATE));
			verify(listener).newRate(any(CurrencyRateEvent.class));
		}
	}

	@Test
	public void getAllRatesFromLocalProvider() {
		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		when(localProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet())).thenReturn(RATES);
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (ChainedCurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			Map<Date, RateValue> rates = chainedProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet());

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
		when(localProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet())).thenReturn(new HashMap<Date, RateValue>());
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		when(remoteProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet())).thenReturn(RATES);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (ChainedCurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			Map<Date, RateValue> rates = chainedProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet());

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
		when(localProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet())).thenReturn(localRates);
		CurrencyRateProvider remoteProvider = mock(CurrencyRateProvider.class);
		when(remoteProvider.getRates(SYMBOL_FROM, SYMBOL_TO, remoteRates.keySet())).thenReturn(remoteRates);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (ChainedCurrencyRateProvider chainedProvider = createChainedProvider(localProvider, remoteProvider, listener)) {
			Map<Date, RateValue> rates = chainedProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet());

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
		when(remoteProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(RATE);
		when(remoteProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet())).thenReturn(RATES);
		ObservableCurrencyRateProviderProxy observableRemoteProvider = new ObservableCurrencyRateProviderProxy(remoteProvider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (ChainedCurrencyRateProvider chainedProvider = createChainedProvider(localProvider, observableRemoteProvider, listener)) {
			RateValue rate = observableRemoteProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE);
			Map<Date, RateValue> rates = observableRemoteProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet());

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
