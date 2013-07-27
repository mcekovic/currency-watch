package org.strangeforest.currencywatch.ui;

public enum Period {

	WEEK("Week", 7),
	TWO_WEEKS("2 Weeks", 14),
	MONTH("Month", 30),
	QUARTER("Quarter", 91),
	TWO_QUARTERS("2 Quarters", 182),
	YEAR("Year", 365),
	TWO_YEARS("2 Years", 730),
	FIVE_YEARS("5 Years", 1826),
	TEN_YEARS("10 Years", 3652),
	MAXIMUM("Maximum");

	private final String label;
	private final int days;

	private Period(String label, int days) {
		this.label = label;
		this.days = days;
	}

	private Period(String label) {
		this(label, UIUtil.daysFromStart());
	}

	public String label() {
		return label;
	}

	public int days() {
		return days;
	}

	@Override public String toString() {
		return label;
	}
}
