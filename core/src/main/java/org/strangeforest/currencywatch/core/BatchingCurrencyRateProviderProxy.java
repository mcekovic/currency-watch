package org.strangeforest.currencywatch.core;

import java.util.*;

public class BatchingCurrencyRateProviderProxy extends ObservableCurrencyRateProviderProxy {

	private int batchSize;

	private static final int BATCH_SIZE = 50;

	public BatchingCurrencyRateProviderProxy(CurrencyRateProvider provider) {
		this(provider, BATCH_SIZE);
	}

	public BatchingCurrencyRateProviderProxy(CurrencyRateProvider provider, int batchSize) {
		super(provider);
		this.batchSize = batchSize;
	}

	@Override public void close() {
		super.close();
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	@Override public Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		if (dates.size() <= batchSize)
			return super.getRates(baseCurrency, currency, dates);
		else {
			Map<Date, RateValue> dateRates = new TreeMap<>();
			List<Date> batchDates = null;
			for (Date date : dates) {
				if (batchDates == null)
					batchDates = new ArrayList<>(batchSize);
				batchDates.add(date);
				if (batchDates.size() == batchSize) {
					dateRates.putAll(super.getRates(baseCurrency, currency, batchDates));
					batchDates = null;
				}
			}
			if (batchDates != null)
				dateRates.putAll(super.getRates(baseCurrency, currency, batchDates));
			return dateRates;
		}
	}
}