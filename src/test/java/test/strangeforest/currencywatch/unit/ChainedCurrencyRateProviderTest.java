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
import org.hamcrest.Matchers;
import org.junit.*;
import org.mockito.*;
import org.strangeforest.currencywatch.core.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChainedCurrencyRateProviderTest {

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
	public static void setUp() {
		RATES.put(DATE1, new RateValue(120.0, 116.0, 118.0));
		RATES.put(DATE2, new RateValue(121.0, 117.0, 119.0));
		RATES.put(DATE3, new RateValue(120.5, 116.5, 118.5));
		RATES.put(DATE4, new RateValue(121.1, 117.1, 119.1));
		RATES.put(DATE5, new RateValue(120.7, 116.7, 118.7));
	}

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
