package org.strangeforest.currencywatch.core;

import java.util.*;

public abstract class BaseUpdatableCurrencyRateProvider extends BaseCurrencyRateProvider implements UpdatableCurrencyRateProvider {

	@Override public void setRates(String symbolFrom, String symbolTo, Map<Date, RateValue> dateRates) throws CurrencyRateException {
		for (Map.Entry<Date, RateValue> dateRate : dateRates.entrySet())
			setRate(symbolFrom, symbolTo, dateRate.getKey(), dateRate.getValue());
	}
}