package org.strangeforest.currencywatch.ui;

public enum CurrencySymbol {

	EUR("Eurozone euro"),
	USD("United States dollar"),
	GBP("British pound sterling"),
	CHF("Swiss franc");

	private final String description;

	private CurrencySymbol(String description) {
		this.description = description;
	}

	public String description() {
		return description;
	}
}
