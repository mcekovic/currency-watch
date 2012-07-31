package org.strangeforest.currencywatch.core;

import java.util.*;

public interface CurrencyRateProvider extends AutoCloseable {

	void init();
	RateValue getRate(String symbolFrom, String symbolTo, Date date);
	Map<Date, RateValue> getRates(String symbolFrom, String symbolTo, Collection<Date> dates);
	@Override void close();
}