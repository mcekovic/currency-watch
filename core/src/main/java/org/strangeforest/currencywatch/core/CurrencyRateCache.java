package org.strangeforest.currencywatch.core;

import java.util.*;
import java.util.concurrent.*;

public class CurrencyRateCache implements UpdatableCurrencyRateProvider {

	private final ConcurrentMap<CurrencyRateKey, CurrencyRates> ratesCache;

	public CurrencyRateCache() {
		super();
		this.ratesCache = new ConcurrentHashMap<>();
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		CurrencyRateKey key = new CurrencyRateKey(baseCurrency, currency);
		CurrencyRates rates = ratesCache.get(key);
		return rates != null ? rates.getRate(date) : null;
	}

	@Override public void setRate(String baseCurrency, String currency, Date date, RateValue rateValue) {
		CurrencyRateKey key = new CurrencyRateKey(baseCurrency, currency);
		CurrencyRates rates = ratesCache.get(key);
		if (rates == null) {
			CurrencyRates newRates = new CurrencyRates(baseCurrency, currency);
			rates = ratesCache.putIfAbsent(key, newRates);
			if (rates == null)
				rates = newRates;
		}
		rates.setRate(date, rateValue);
	}

	private static final class CurrencyRateKey {

		private final String baseCurrency;
		private final String currency;

		public CurrencyRateKey(String baseCurrency, String currency) {
			this.baseCurrency = baseCurrency;
			this.currency = currency;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof CurrencyRateKey)) return false;
			CurrencyRateKey key = (CurrencyRateKey)o;
			return baseCurrency.equals(key.baseCurrency) && currency.equals(key.currency);
		}

		@Override public int hashCode() {
			return 31 * baseCurrency.hashCode() + currency.hashCode();
		}
	}
}
