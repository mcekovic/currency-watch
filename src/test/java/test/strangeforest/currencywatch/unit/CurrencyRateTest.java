package test.strangeforest.currencywatch.unit;

import java.util.*;

import org.hamcrest.*;
import org.junit.*;
import org.strangeforest.currencywatch.core.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRateTest {

	@Test
	public void getRateFromProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(provider, listener)) {
			RateValue rate = currencyRate.getRate(DATE);

			assertEquals(RATE, rate);
			verify(listener).newRate(any(CurrencyRateEvent.class));

			RateValue cachedRate = currencyRate.getRate(DATE);

			assertEquals(RATE, cachedRate);
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getRatesFromProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(SYMBOL_FROM, SYMBOL_TO, new ArrayList<>(DATES))).thenReturn(RATES);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(provider, listener)) {
			Map<Date, RateValue> rates = currencyRate.getRates(DATES);

			assertEquals(RATES, rates);
			verify(listener).newRates(argThat(matchesEventCount(5)));

			Map<Date, RateValue> cachedRates = currencyRate.getRates(DATES);

			assertEquals(RATES, cachedRates);
			assertEquals(RATES, currencyRate.getRates());
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getRateObservableFromProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(RATE);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(observableProvider, listener)) {
			RateValue rate = currencyRate.getRate(DATE);

			assertEquals(RATE, rate);
			verify(listener).newRate(any(CurrencyRateEvent.class));

			RateValue cachedRate = currencyRate.getRate(DATE);

			assertEquals(RATE, cachedRate);
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getRatesFromObservableProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(SYMBOL_FROM, SYMBOL_TO, new ArrayList<>(DATES))).thenReturn(RATES);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(observableProvider, listener)) {
			Map<Date, RateValue> rates = currencyRate.getRates(DATES);

			assertEquals(RATES, rates);
			verify(listener).newRates(argThat(matchesEventCount(5)));

			Map<Date, RateValue> cachedRates = currencyRate.getRates(DATES);

			assertEquals(RATES, cachedRates);
			assertEquals(RATES, currencyRate.getRates());
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void rateIsPropagatedFromObservableProviderOnlyOnceAndCached() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(RATE);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(observableProvider, listener)) {
			RateValue rate = observableProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE);

			assertEquals(RATE, rate);
			verify(listener).newRate(any(CurrencyRateEvent.class));

			RateValue cachedRate = currencyRate.getRate(DATE);
			RateValue rate2 = observableProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE);

			assertEquals(RATE, cachedRate);
			assertEquals(RATE, rate2);
			verify(provider, times(2)).getRate(SYMBOL_FROM, SYMBOL_TO, DATE);
			verifyNoMoreInteractions(provider);
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void ratesArePropagatedFromObservableProviderOnlyOnceAndCached() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(SYMBOL_FROM, SYMBOL_TO, DATES)).thenReturn(RATES);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(observableProvider, listener)) {
			Map<Date, RateValue> rates = observableProvider.getRates(SYMBOL_FROM, SYMBOL_TO, DATES);

			assertEquals(RATES, rates);
			verify(listener).newRates(argThat(matchesEventCount(5)));

			Map<Date, RateValue> cachedRates = currencyRate.getRates(DATES);
			Map<Date, RateValue> rates2 = observableProvider.getRates(SYMBOL_FROM, SYMBOL_TO, DATES);

			assertEquals(RATES, cachedRates);
			assertEquals(RATES, rates2);
			verify(provider, times(2)).getRates(SYMBOL_FROM, SYMBOL_TO, DATES);
			verifyNoMoreInteractions(provider);
			verifyNoMoreInteractions(listener);
		}
	}

	private CurrencyRate createCurrencyRate(CurrencyRateProvider provider, CurrencyRateListener listener) {
		CurrencyRate currencyRate = new CurrencyRate(SYMBOL_FROM, SYMBOL_TO, provider);
		currencyRate.addListener(listener);
		return currencyRate;
	}

	private static Matcher<CurrencyRateEvent[]> matchesEventCount(int count) {
		return Matchers.arrayWithSize(count);
	}
}
