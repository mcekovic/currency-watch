package org.strangeforest.currencywatch.core;

import java.util.*;

public interface UpdatableCurrencyRateProvider extends CurrencyRateProvider {

	void setRate(String baseCurrency, String currency, Date date, RateValue rateValue);
	void setRates(String baseCurrency, String currency, Map<Date, RateValue> dateRates);
}