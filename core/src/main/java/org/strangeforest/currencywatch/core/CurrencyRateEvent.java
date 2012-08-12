package org.strangeforest.currencywatch.core;

import java.util.*;

public class CurrencyRateEvent extends EventObject {

	private final String baseCurrency;
	private final String currency;
	private final Date date;
	private final RateValue rate;

	public CurrencyRateEvent(Object source, String baseCurrency, String currency, Date date, RateValue rate) {
		super(source);
		this.baseCurrency = baseCurrency;
		this.currency = currency;
		this.date = date;
		this.rate = rate;
	}

	public String getBaseCurrency() {
		return baseCurrency;
	}

	public String getCurrency() {
		return currency;
	}

	public Date getDate() {
		return date;
	}

	public RateValue getRate() {
		return rate;
	}

	@Override public String toString() {
		return String.format("CurrencyRateEvent{baseCurrency=%1$s, currency=%2$s, date=%3$td-%3$tm-%3$tY, rate=%4$s}", baseCurrency, currency, date, rate);
	}
}