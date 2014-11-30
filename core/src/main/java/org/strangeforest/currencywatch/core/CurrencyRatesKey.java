package org.strangeforest.currencywatch.core;

import java.io.*;

public final class CurrencyRatesKey implements Serializable {

	private String baseCurrency;
	private String currency;

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

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeUTF(baseCurrency);
		out.writeUTF(currency);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		baseCurrency = in.readUTF();
		currency = in.readUTF();
	}
}
