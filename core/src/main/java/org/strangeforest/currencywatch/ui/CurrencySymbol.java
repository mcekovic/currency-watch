package org.strangeforest.currencywatch.ui;

public enum CurrencySymbol {

	EUR("Eurozone Euro"),
	USD("United States Dollar"),
	GBP("British Pound Sterling"),
	CHF("Swiss Franc");

	private final String description;

	private CurrencySymbol(String description) {
		this.description = description;
	}

	public String description() {
		return description;
	}
}
