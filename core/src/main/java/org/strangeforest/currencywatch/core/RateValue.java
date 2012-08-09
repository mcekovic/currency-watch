package org.strangeforest.currencywatch.core;

public class RateValue {

	private final double bid;
	private final double ask;
	private final double middle;

	public RateValue(double bid, double ask, double middle) {
		super();
		this.bid = bid;
		this.ask = ask;
		this.middle = middle;
	}

	public double getBid() {
		return bid;
	}

	public double getAsk() {
		return ask;
	}

	public double getMiddle() {
		return middle;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof RateValue)) return false;
		RateValue rateValue = (RateValue)o;
		return bid == rateValue.bid && ask == rateValue.ask && middle == rateValue.middle;
	}

	@Override public int hashCode() {
		long temp = bid != +0.0d ? Double.doubleToLongBits(bid) : 0L;
		int result = (int)(temp ^ (temp >>> 32));
		temp = ask != +0.0d ? Double.doubleToLongBits(ask) : 0L;
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		temp = middle != +0.0d ? Double.doubleToLongBits(middle) : 0L;
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		return result;
	}

	@Override public String toString() {
		return String.format("RateValue{bid=%1$f, ask=%2$f, middle=%3$f}", bid, ask, middle);
	}
}