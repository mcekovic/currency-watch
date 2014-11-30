package org.strangeforest.currencywatch.mapdb;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;

public class CurrencyRatesValue extends BaseCurrencyRates implements Serializable {

	private final Map<Date, RateValue> dateRates;

	public CurrencyRatesValue(String baseCurrency, String currency, Map<Date, RateValue> dateRates) {
		super(baseCurrency, currency);
		this.dateRates = dateRates;
	}

	@Override public RateValue getRate(Date date) {
		return dateRates.get(date);
	}

	@Override public Map<Date, RateValue> getRates() {
		return dateRates;
	}
}