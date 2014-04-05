package org.strangeforest.currencywatch.core;

import java.util.*;

public interface UpdatableCurrencyRateProvider extends CurrencyRateProvider {

	void setRate(String baseCurrency, String currency, Date date, RateValue rateValue);

	default void setRates(String baseCurrency, String currency, Map<Date, RateValue> dateRates) {
		for (Map.Entry<Date, RateValue> dateRate : dateRates.entrySet())
			setRate(baseCurrency, currency, dateRate.getKey(), dateRate.getValue());
	}
}