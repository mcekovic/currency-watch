package org.strangeforest.currencywatch.core;

import java.util.*;

public abstract class BaseCurrencyRateProvider implements CurrencyRateProvider {

	@Override public void init() {}

	@Override public void close() {}

	@Override public Map<Date, RateValue> getRates(String symbolFrom, String symbolTo, Collection<Date> dates) {
		Map<Date, RateValue> dateRates = new TreeMap<>();
		for (Date date : dates) {
			RateValue rateValue = getRate(symbolFrom, symbolTo, date);
			if (rateValue != null)
				dateRates.put(date, rateValue);
		}
		return dateRates;
	}
}