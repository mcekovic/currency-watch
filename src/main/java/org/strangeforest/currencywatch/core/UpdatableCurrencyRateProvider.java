package org.strangeforest.currencywatch.core;

import java.util.*;

public interface UpdatableCurrencyRateProvider extends CurrencyRateProvider {

	void setRate(String symbolFrom, String symbolTo, Date date, RateValue rateValue) throws CurrencyRateException;
	void setRates(String symbolFrom, String symbolTo, Map<Date, RateValue> dateRates) throws CurrencyRateException;
}