package org.strangeforest.currencywatch.core;

import java.util.*;

import com.finsoft.util.*;

public interface CurrencyRateProvider extends Disposable {

	void init() throws CurrencyRateException;
	RateValue getRate(String symbolFrom, String symbolTo, Date date) throws CurrencyRateException;
	Map<Date, RateValue> getRates(String symbolFrom, String symbolTo, Collection<Date> dates) throws CurrencyRateException;
}