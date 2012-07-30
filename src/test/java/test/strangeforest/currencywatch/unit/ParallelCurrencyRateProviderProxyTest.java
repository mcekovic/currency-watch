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

import org.mockito.invocation.*;
import org.mockito.stubbing.*;
import org.strangeforest.currencywatch.core.*;
import org.testng.*;
import org.testng.annotations.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class ParallelCurrencyRateProviderProxyTest {

	private static final String SYMBOL_FROM = "DIN";
	private static final String SYMBOL_TO = "EUR";
	private static final Date DATE = new GregorianCalendar(2012, 4, 1).getTime();
	private static final RateValue RATE = new RateValue(120.0, 116.0, 118.0);
	private static final Map<Date,RateValue> RATES = new HashMap<>();

	@BeforeClass
	public void setUp() {
		RATES.put(new GregorianCalendar(2012, 4, 1).getTime(), new RateValue(120.0, 116.0, 118.0));
		RATES.put(new GregorianCalendar(2012, 4, 2).getTime(), new RateValue(121.0, 117.0, 119.0));
		RATES.put(new GregorianCalendar(2012, 4, 3).getTime(), new RateValue(120.5, 116.5, 118.5));
		RATES.put(new GregorianCalendar(2012, 4, 4).getTime(), new RateValue(121.1, 117.1, 119.1));
		RATES.put(new GregorianCalendar(2012, 4, 5).getTime(), new RateValue(120.7, 116.7, 118.7));
	}

	@Test
	public void getRate() throws CurrencyRateException {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		ParallelCurrencyRateProviderProxy parallelProvider = new ParallelCurrencyRateProviderProxy(provider, 1);
		parallelProvider.addListener(listener);
		RateValue rate = parallelProvider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE);

		assertEquals(rate, RATE);
		verify(listener).newRate(any(CurrencyRateEvent.class));
	}

	@Test
	public void getRates() throws CurrencyRateException {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(eq(SYMBOL_FROM), eq(SYMBOL_TO), any(Date.class))).thenAnswer(new Answer<Object>() {
			@Override public Object answer(InvocationOnMock invocation) throws Throwable {
				return RATES.get(invocation.getArguments()[2]);
			}
		});
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		ParallelCurrencyRateProviderProxy parallelProvider = new ParallelCurrencyRateProviderProxy(provider, 2);
		parallelProvider.addListener(listener);
		Map<Date, RateValue> rates = parallelProvider.getRates(SYMBOL_FROM, SYMBOL_TO, RATES.keySet());

		assertEquals(rates, RATES);
		verify(listener, times(RATES.size())).newRate(any(CurrencyRateEvent.class));
	}

	@Test
	public void getRatesRetried() throws CurrencyRateException {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenThrow(new CurrencyRateException("Booom!")).thenReturn(RATE);
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		ParallelCurrencyRateProviderProxy parallelProvider = new ParallelCurrencyRateProviderProxy(provider, 1);
		parallelProvider.addListener(listener);
		Map<Date, RateValue> rates = parallelProvider.getRates(SYMBOL_FROM, SYMBOL_TO, Collections.singleton(DATE));

		assertEquals(rates, Collections.singletonMap(DATE, RATE));
		verify(provider, times(2)).getRate(SYMBOL_FROM, SYMBOL_TO, DATE);
		verify(listener).newRate(any(CurrencyRateEvent.class));
	}

	@Test
	public void getRatesRetryFail() throws CurrencyRateException {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRate(SYMBOL_FROM, SYMBOL_TO, DATE)).thenThrow(new CurrencyRateException("Booom!"));
		CurrencyRateListener listener = mock(CurrencyRateListener.class);

		ParallelCurrencyRateProviderProxy parallelProvider = new ParallelCurrencyRateProviderProxy(provider, 1);
		parallelProvider.addListener(listener);
		try {
			parallelProvider.getRates(SYMBOL_FROM, SYMBOL_TO, Collections.singleton(DATE));
			Assert.fail("Exception is not thrown.");
		}
		catch (CurrencyRateException ex) {
			verify(provider, times(3)).getRate(SYMBOL_FROM, SYMBOL_TO, DATE);
			verify(listener, never()).newRate(any(CurrencyRateEvent.class));
		}
	}
}
