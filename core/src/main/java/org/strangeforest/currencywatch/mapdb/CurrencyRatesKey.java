package org.strangeforest.currencywatch.mapdb;

import java.io.*;

public class CurrencyRatesKey implements Serializable {

	private final String baseCurrency;
	private final String currency;

	public CurrencyRatesKey(String baseCurrency, String currency) {
		this.baseCurrency = baseCurrency;
		this.currency = currency;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CurrencyRatesKey key = (CurrencyRatesKey)o;
		return baseCurrency.equals(key.baseCurrency) && currency.equals(key.currency);
	}

	@Override public int hashCode() {
		return 31 * baseCurrency.hashCode() + currency.hashCode();
	}
}
