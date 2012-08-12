package org.strangeforest.currencywatch.core;

import java.util.*;

public interface CurrencyRateProvider extends AutoCloseable {

	void init();
	RateValue getRate(String baseCurrency, String currency, Date date);
	Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates);
	@Override void close();
}