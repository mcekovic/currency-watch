package org.strangeforest.currencywatch.core;

import java.util.*;

public abstract class BaseCurrencyRate {

	protected final String symbolFrom;
	protected final String symbolTo;

	protected BaseCurrencyRate(String symbolFrom, String symbolTo) {
		super();
		this.symbolFrom = symbolFrom;
		this.symbolTo = symbolTo;
	}

	public abstract RateValue getRate(Date date);
	public abstract Map<Date, RateValue> getRates();

	public Map<Date, RateValue> getRates(Iterable<Date> dates) {
		Map<Date, RateValue> dateRates = new TreeMap<Date, RateValue>();
		for (Date date : dates) {
			RateValue rateValue = getRate(date);
			if (rateValue != null)
				dateRates.put(date, rateValue);
		}
		return dateRates;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof BaseCurrencyRate)) return false;
		BaseCurrencyRate rate = (BaseCurrencyRate)o;
		return symbolFrom.equals(rate.symbolFrom) && symbolTo.equals(rate.symbolTo);
	}

	@Override public int hashCode() {
		return 31 * symbolFrom.hashCode() + symbolTo.hashCode();
	}

	@Override public String toString() {
		return String.format("CurrencyRate{symbolFrom=%1$s, symbolTo=%2$s, rates=%3$s}", symbolFrom, symbolTo, getRates());
	}
}