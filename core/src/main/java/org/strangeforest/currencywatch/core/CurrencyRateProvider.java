package org.strangeforest.currencywatch.core;

import java.util.*;

public interface CurrencyRateProvider extends AutoCloseable {

	default void init() {}
	@Override default void close() {}

	RateValue getRate(String baseCurrency, String currency, Date date);

	default Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		Map<Date, RateValue> dateRates = new TreeMap<>();
		for (Date date : dates) {
			RateValue rateValue = getRate(baseCurrency, currency, date);
			if (rateValue != null)
				dateRates.put(date, rateValue);
		}
		return dateRates;
	}
}