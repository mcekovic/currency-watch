package org.strangeforest.currencywatch.core;

public class RateValue {

	private final double ask;
	private final double bid;
	private final double middle;

	public RateValue(double ask, double bid, double middle) {
		super();
		this.ask = ask;
		this.bid = bid;
		this.middle = middle;
	}

	public double getAsk() {
		return ask;
	}

	public double getBid() {
		return bid;
	}

	public double getMiddle() {
		return middle;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof RateValue)) return false;
		RateValue rateValue = (RateValue)o;
		return ask == rateValue.ask && bid == rateValue.bid && middle == rateValue.middle;
	}

	@Override public int hashCode() {
		long temp = ask != +0.0d ? Double.doubleToLongBits(ask) : 0L;
		int result = (int)(temp ^ (temp >>> 32));
		temp = bid != +0.0d ? Double.doubleToLongBits(bid) : 0L;
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		temp = middle != +0.0d ? Double.doubleToLongBits(middle) : 0L;
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		return result;
	}

	@Override public String toString() {
		return String.format("RateValue{ask=%1$f, bid=%2$f, middle=%3$f}", ask, bid, middle);
	}
}