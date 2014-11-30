package test.strangeforest.currencywatch.unit.core;

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
		when(provider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRates currencyRates = createCurrencyRates(provider, listener)) {
			RateValue rate = currencyRates.getRate(DATE);

			assertEquals(RATE, rate);
			verify(listener).newRate(any(CurrencyRateEvent.class));

			RateValue cachedRate = currencyRates.getRate(DATE);

			assertEquals(RATE, cachedRate);
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getRatesFromProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(BASE_CURRENCY, CURRENCY, DATE_LIST)).thenReturn(RATES);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRates currencyRates = createCurrencyRates(provider, listener)) {
			Map<Date, RateValue> rates = currencyRates.getRates(DATES);

			assertEquals(RATES, rates);
			verify(listener).newRates(argThat(matchesEventCount(5)));

			Map<Date, RateValue> cachedRates = currencyRates.getRates(DATES);

			assertEquals(RATES, cachedRates);
			assertEquals(RATES, currencyRates.getRates());
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getRateObservableFromProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRates currencyRates = createCurrencyRates(observableProvider, listener)) {
			RateValue rate = currencyRates.getRate(DATE);

			assertEquals(RATE, rate);
			verify(listener).newRate(any(CurrencyRateEvent.class));

			RateValue cachedRate = currencyRates.getRate(DATE);

			assertEquals(RATE, cachedRate);
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getRatesFromObservableProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(BASE_CURRENCY, CURRENCY, DATE_LIST)).thenReturn(RATES);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRates currencyRates = createCurrencyRates(observableProvider, listener)) {
			Map<Date, RateValue> rates = currencyRates.getRates(DATES);

			assertEquals(RATES, rates);
			verify(listener).newRates(argThat(matchesEventCount(5)));

			Map<Date, RateValue> cachedRates = currencyRates.getRates(DATES);

			assertEquals(RATES, cachedRates);
			assertEquals(RATES, currencyRates.getRates());
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void rateIsPropagatedFromObservableProviderOnlyOnceAndCached() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(BASE_CURRENCY, CURRENCY, DATE)).thenReturn(RATE);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRates currencyRates = createCurrencyRates(observableProvider, listener)) {
			RateValue rate = observableProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);

			assertEquals(RATE, rate);
			verify(listener).newRate(any(CurrencyRateEvent.class));

			RateValue cachedRate = currencyRates.getRate(DATE);
			RateValue rate2 = observableProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);

			assertEquals(RATE, cachedRate);
			assertEquals(RATE, rate2);
			verify(provider, times(2)).getRate(BASE_CURRENCY, CURRENCY, DATE);
			verifyNoMoreInteractions(provider);
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void ratesArePropagatedFromObservableProviderOnlyOnceAndCached() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(BASE_CURRENCY, CURRENCY, DATES)).thenReturn(RATES);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRates currencyRates = createCurrencyRates(observableProvider, listener)) {
			Map<Date, RateValue> rates = observableProvider.getRates(BASE_CURRENCY, CURRENCY, DATES);

			assertEquals(RATES, rates);
			verify(listener).newRates(argThat(matchesEventCount(5)));

			Map<Date, RateValue> cachedRates = currencyRates.getRates(DATES);
			Map<Date, RateValue> rates2 = observableProvider.getRates(BASE_CURRENCY, CURRENCY, DATES);

			assertEquals(RATES, cachedRates);
			assertEquals(RATES, rates2);
			verify(provider, times(2)).getRates(BASE_CURRENCY, CURRENCY, DATES);
			verifyNoMoreInteractions(provider);
			verifyNoMoreInteractions(listener);
		}
	}

	private CurrencyRates createCurrencyRates(CurrencyRateProvider provider, CurrencyRateListener listener) {
		CurrencyRates currencyRates = new CurrencyRates(BASE_CURRENCY, CURRENCY, provider);
		currencyRates.addListener(listener);
		return currencyRates;
	}

	private static Matcher<CurrencyRateEvent[]> matchesEventCount(int count) {
		return Matchers.arrayWithSize(count);
	}
}
