/******************************************************************************/
/* (C) Copyright (and database rights) 1995-2011 Finsoft Ltd., United Kingdom */
/*                                                                            */
/* ALL RIGHTS RESERVED                                                        */
/*                                                                            */
/* The software and information contained herein are proprietary to, and      */
/* comprise valuable trade secrets of, Finsoft Ltd., which intends to         */
/* preserve as trade secrets such software and information. This software     */
/* is furnished pursuant to a written license agreement and may be used,      */
/* copied, transmitted, and stored only in accordance with the terms of       */
/* such license and with the inclusion of the above copyright notice.         */
/* This software and information or any other copies thereof may not be       */
/* provided or otherwise made available to any other person.                  */
/*                                                                            */
/******************************************************************************/

package test.strangeforest.currencywatch.unit;

import java.util.*;

import org.hamcrest.*;
import org.strangeforest.currencywatch.core.*;
import org.testng.annotations.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class CurrencyRateTest {

	private static final String SYMBOL_FROM = "DIN";
	private static final String SYMBOL_TO = "EUR";
	private static final Date DATE = new GregorianCalendar(2012, 4, 1).getTime();
	private static final RateValue RATE = new RateValue(120.0, 116.0, 118.0);

	private static final NavigableMap<Date,RateValue> RATES = new TreeMap<>();
	private static final Date DATE1 = new GregorianCalendar(2012, 4, 1).getTime();
	private static final Date DATE2 = new GregorianCalendar(2012, 4, 2).getTime();
	private static final Date DATE3 = new GregorianCalendar(2012, 4, 3).getTime();
	private static final Date DATE4 = new GregorianCalendar(2012, 4, 4).getTime();
	private static final Date DATE5 = new GregorianCalendar(2012, 4, 5).getTime();

	@BeforeClass
	public void setUp() {
		RATES.put(DATE1, new RateValue(120.0, 116.0, 118.0));
		RATES.put(DATE2, new RateValue(121.0, 117.0, 119.0));
		RATES.put(DATE3, new RateValue(120.5, 116.5, 118.5));
		RATES.put(DATE4, new RateValue(121.1, 117.1, 119.1));
		RATES.put(DATE5, new RateValue(120.7, 116.7, 118.7));
	}

	@Test
	public void getRateFromProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(provider, listener)) {
			RateValue rate = currencyRate.getRate(DATE);

			assertEquals(rate, RATE);
			verify(listener).newRate(any(CurrencyRateEvent.class));

			RateValue cachedRate = currencyRate.getRate(DATE);

			assertEquals(cachedRate, RATE);
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getRatesFromProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(SYMBOL_FROM, SYMBOL_TO, new ArrayList<>(RATES.keySet()))).thenReturn(RATES);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(provider, listener)) {
			Map<Date, RateValue> rates = currencyRate.getRates(RATES.keySet());

			assertEquals(rates, RATES);
			verify(listener).newRates(argThat(matchesEventCount(5)));

			Map<Date, RateValue> cachedRates = currencyRate.getRates(RATES.keySet());

			assertEquals(cachedRates, RATES);
			assertEquals(currencyRate.getRates(), RATES);
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

			assertEquals(rate, RATE);
			verify(listener).newRate(any(CurrencyRateEvent.class));

			RateValue cachedRate = currencyRate.getRate(DATE);

			assertEquals(cachedRate, RATE);
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void getRatesFromObservableProvider() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(SYMBOL_FROM, SYMBOL_TO, new ArrayList<>(RATES.keySet()))).thenReturn(RATES);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(observableProvider, listener)) {
			Map<Date, RateValue> rates = currencyRate.getRates(RATES.keySet());

			assertEquals(rates, RATES);
			verify(listener).newRates(argThat(matchesEventCount(5)));

			Map<Date, RateValue> cachedRates = currencyRate.getRates(RATES.keySet());

			assertEquals(cachedRates, RATES);
			assertEquals(currencyRate.getRates(), RATES);
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

			assertEquals(rate, RATE);
			verify(listener).newRate(any(CurrencyRateEvent.class));

			RateValue cachedRate = currencyRate.getRate(DATE);
			RateValue rate2 = observableProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE);

			assertEquals(cachedRate, RATE);
			assertEquals(rate2, RATE);
			verify(provider, times(2)).getRate(SYMBOL_FROM, SYMBOL_TO, DATE);
			verifyNoMoreInteractions(provider);
			verifyNoMoreInteractions(listener);
		}
	}

	@Test
	public void ratesArePropagatedFromObservableProviderOnlyOnceAndCached() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet())).thenReturn(RATES);
		ObservableCurrencyRateProviderProxy observableProvider = new ObservableCurrencyRateProviderProxy(provider);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		try (CurrencyRate currencyRate = createCurrencyRate(observableProvider, listener)) {
			Map<Date, RateValue> rates = observableProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet());

			assertEquals(rates, RATES);
			verify(listener).newRates(argThat(matchesEventCount(5)));

			Map<Date, RateValue> cachedRates = currencyRate.getRates(RATES.keySet());
			Map<Date, RateValue> rates2 = observableProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet());

			assertEquals(cachedRates, RATES);
			assertEquals(rates2, RATES);
			verify(provider, times(2)).getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet());
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
