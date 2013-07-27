package org.strangeforest.currencywatch.core;

import java.util.*;

public class CurrencyEvent {

	private final String currency;
	private final Date date;
	private final String title;
	private final String description;

	public CurrencyEvent(String currency, Date date, String title, String description) {
		super();
		this.currency = currency;
		this.date = date;
		this.title = title;
		this.description = description;
	}

	public String getCurrency() {
		return currency;
	}

	public Date getDate() {
		return date;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	@Override public String toString() {
		return String.format("CurrencyEvent{currency=%1$s, date=%2$td-%2$tm-%2$tY, title=%3$s}", currency, date, title);
	}
}
