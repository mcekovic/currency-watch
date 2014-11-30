package org.strangeforest.currencywatch.core;

import java.util.*;
import java.util.concurrent.*;

public class CurrencyRateCache implements UpdatableCurrencyRateProvider {

	private final ConcurrentMap<CurrencyRatesKey, CurrencyRates> ratesCache;

	public CurrencyRateCache() {
		super();
		this.ratesCache = new ConcurrentHashMap<>();
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		CurrencyRatesKey key = new CurrencyRatesKey(baseCurrency, currency);
		CurrencyRates rates = ratesCache.get(key);
		return rates != null ? rates.getRate(date) : null;
	}

	@Override public void setRate(String baseCurrency, String currency, Date date, RateValue rateValue) {
		CurrencyRatesKey key = new CurrencyRatesKey(baseCurrency, currency);
		CurrencyRates rates = ratesCache.get(key);
		if (rates == null) {
			CurrencyRates newRates = new CurrencyRates(baseCurrency, currency);
			rates = ratesCache.putIfAbsent(key, newRates);
			if (rates == null)
				rates = newRates;
		}
		rates.setRate(date, rateValue);
	}
}
