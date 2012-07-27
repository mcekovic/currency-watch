package org.strangeforest.currencywatch.core;

import java.util.*;

public class CurrencyRateEvent extends EventObject {

	private final String symbolFrom;
	private final String symbolTo;
	private final Date date;
	private final RateValue rate;

	public CurrencyRateEvent(Object source, String symbolFrom, String symbolTo, Date date, RateValue rate) {
		super(source);
		this.symbolFrom = symbolFrom;
		this.symbolTo = symbolTo;
		this.date = date;
		this.rate = rate;
	}

	public String getSymbolFrom() {
		return symbolFrom;
	}

	public String getSymbolTo() {
		return symbolTo;
	}

	public Date getDate() {
		return date;
	}

	public RateValue getRate() {
		return rate;
	}

	@Override public String toString() {
		return String.format("CurrencyRateEvent{symbolFrom=%1$s, symbolTo=%2$s, date=%3$tY-%3$tm-%3$td, rate=%4$s}", symbolFrom, symbolTo, date, rate);
	}
}