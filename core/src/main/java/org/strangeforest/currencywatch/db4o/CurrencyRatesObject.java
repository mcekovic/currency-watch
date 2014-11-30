package org.strangeforest.currencywatch.db4o;

import java.util.*;

import org.strangeforest.currencywatch.core.*;

public class CurrencyRatesObject extends BaseCurrencyRates {

	private final Map<Date, RateValue> dateRates;

	public CurrencyRatesObject(String baseCurrency, String currency) {
		super(baseCurrency, currency);
		dateRates = new HashMap<>();
	}

	@Override public RateValue getRate(Date date) {
		return dateRates.get(date);
	}

	public void setRate(Date date, RateValue rateValue) {
		if (rateValue != null)
			dateRates.put(date, rateValue);
	}

	@Override public Map<Date, RateValue> getRates() {
		return new TreeMap<>(dateRates);
	}

	public void setRates(Map<Date, RateValue> dateRates) {
		for (Map.Entry<Date, RateValue> dateRate : dateRates.entrySet())
			setRate(dateRate.getKey(), dateRate.getValue());
	}
}