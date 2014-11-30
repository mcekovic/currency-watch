package org.strangeforest.currencywatch.core;

import java.io.*;
import java.util.*;

public abstract class BaseCurrencyRates implements Serializable {

	protected String baseCurrency;
	protected String currency;

	protected BaseCurrencyRates(String baseCurrency, String currency) {
		super();
		this.baseCurrency = baseCurrency;
		this.currency = currency;
	}

	public String getBaseCurrency() {
		return baseCurrency;
	}

	public String getCurrency() {
		return currency;
	}

	public abstract RateValue getRate(Date date);
	public abstract Map<Date, RateValue> getRates();

	public Map<Date, RateValue> getRates(Iterable<Date> dates) {
		Map<Date, RateValue> dateRates = new TreeMap<>();
		for (Date date : dates) {
			RateValue rateValue = getRate(date);
			if (rateValue != null)
				dateRates.put(date, rateValue);
		}
		return dateRates;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BaseCurrencyRates)) return false;
		BaseCurrencyRates rates = (BaseCurrencyRates)o;
		return baseCurrency.equals(rates.baseCurrency) && currency.equals(rates.currency);
	}

	@Override public int hashCode() {
		return 31 * baseCurrency.hashCode() + currency.hashCode();
	}

	@Override public String toString() {
		return String.format("CurrencyRates{baseCurrency=%1$s, currency=%2$s, rates=%3$s}", baseCurrency, currency, getRates());
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeUTF(baseCurrency);
		out.writeUTF(currency);
	}

	private void readObject(ObjectInputStream in) throws IOException {
		baseCurrency = in.readUTF();
		currency = in.readUTF();
	}
}