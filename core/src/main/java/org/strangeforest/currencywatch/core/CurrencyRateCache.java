package org.strangeforest.currencywatch.core;

import java.util.*;
import java.util.concurrent.*;

public class CurrencyRateCache extends BaseUpdatableCurrencyRateProvider {

	private final ConcurrentMap<CurrencyRateKey, CurrencyRate> rateCache;

	public CurrencyRateCache() {
		super();
		this.rateCache = new ConcurrentHashMap<>();
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		CurrencyRateKey key = new CurrencyRateKey(baseCurrency, currency);
		CurrencyRate rate = rateCache.get(key);
		return rate != null ? rate.getRate(date) : null;
	}

	@Override public void setRate(String baseCurrency, String currency, Date date, RateValue rateValue) {
		CurrencyRateKey key = new CurrencyRateKey(baseCurrency, currency);
		CurrencyRate rate = rateCache.get(key);
		if (rate == null) {
			CurrencyRate newRate = new CurrencyRate(baseCurrency, currency);
			rate = rateCache.putIfAbsent(key, newRate);
			if (rate == null)
				rate = newRate;
		}
		rate.setRate(date, rateValue);
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
