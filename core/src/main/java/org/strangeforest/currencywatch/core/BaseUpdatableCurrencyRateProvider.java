package org.strangeforest.currencywatch.core;

import java.util.*;

public abstract class BaseUpdatableCurrencyRateProvider extends BaseCurrencyRateProvider implements UpdatableCurrencyRateProvider {

	@Override public void setRates(String baseCurrency, String currency, Map<Date, RateValue> dateRates) {
		for (Map.Entry<Date, RateValue> dateRate : dateRates.entrySet())
			setRate(baseCurrency, currency, dateRate.getKey(), dateRate.getValue());
	}
}