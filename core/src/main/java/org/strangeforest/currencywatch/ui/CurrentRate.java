package org.strangeforest.currencywatch.ui;

import java.util.*;

import org.strangeforest.currencywatch.core.*;

public class CurrentRate {

	private final Date date;
	private final RateValue rate;
	private final int direction;

	private static final String GREEN = "#00A000";
	private static final String RED   = "#F00000";
	private static final String BLACK = "#000000";

	public static CurrentRate forRates(Map<Date, RateValue> rates) {
		if (rates.isEmpty())
			return null;
		NavigableMap<Date, RateValue> navRates = rates instanceof NavigableMap ? (NavigableMap<Date, RateValue>)rates : new TreeMap<>(rates);
		Date date = navRates.lastKey();
		RateValue rate = rates.get(date);
		Date prevDate = navRates.lowerKey(date);
		int direction = prevDate != null ? rate.getMiddle().compareTo(rates.get(prevDate).getMiddle()) : 0;
		return new CurrentRate(date, rate, direction);
	}

	private CurrentRate(Date date, RateValue rate, int direction) {
		this.date = date;
		this.rate = rate;
		this.direction = direction;
	}

	public Date getDate() {
		return date;
	}

	public RateValue getRate() {
		return rate;
	}

	public int getDirection() {
		return direction;
	}

	public String getColor() {
		return direction > 0 ? RED : (direction < 0 ? GREEN : BLACK);
	}
}
