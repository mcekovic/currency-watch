package org.strangeforest.currencywatch.mapdb;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;

import static org.strangeforest.currencywatch.mapdb.SerializationUtil.*;

public final class CurrencyRatesValue extends BaseCurrencyRates {

	private Map<Date, RateValue> dateRates;

	public CurrencyRatesValue(String baseCurrency, String currency, Map<Date, RateValue> dateRates) {
		super(baseCurrency, currency);
		this.dateRates = dateRates;
	}

	public RateValue getRate(Date date) {
		return dateRates.get(date);
	}

	public Map<Date, RateValue> getRates() {
		return Collections.unmodifiableMap(dateRates);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CurrencyRatesValue rates = (CurrencyRatesValue)o;
		return dateRates.equals(rates.dateRates);
	}

	@Override public int hashCode() {
		return 31 * super.hashCode() + dateRates.hashCode();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dateRates.size());
		for (Map.Entry<Date, RateValue> dateRate : dateRates.entrySet()) {
			Date date = dateRate.getKey();
			RateValue rate = dateRate.getValue();
			out.writeLong(date.getTime());
			writeDecimal(out, rate.getBid());
			writeDecimal(out, rate.getAsk());
			writeDecimal(out, rate.getMiddle());
		}
	}

	private void readObject(ObjectInputStream in) throws IOException {
		int size = in.readInt();
		dateRates = new HashMap<>(size);
		for (int i = 0; i < size; i++) {
			Date date = new Date(in.readLong());
			RateValue rate = new RateValue(readDecimal(in), readDecimal(in), readDecimal(in));
			dateRates.put(date, rate);
		}
	}
}