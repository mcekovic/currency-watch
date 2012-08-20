package org.strangeforest.currencywatch.core;

import java.util.*;

public class UpdatableChainedCurrencyRateProvider extends ChainedCurrencyRateProvider implements UpdatableCurrencyRateProvider {

	private final UpdatableCurrencyRateProvider updatableRemoteProvider;

	public UpdatableChainedCurrencyRateProvider(UpdatableCurrencyRateProvider localProvider, UpdatableCurrencyRateProvider remoteProvider) {
		super(localProvider, remoteProvider);
		updatableRemoteProvider = remoteProvider;
	}

	@Override public void setRate(String baseCurrency, String currency, Date date, RateValue rateValue) {
		localProvider.setRate(baseCurrency, currency, date, rateValue);
		updatableRemoteProvider.setRate(baseCurrency, currency, date, rateValue);
	}

	@Override public void setRates(String baseCurrency, String currency, Map<Date, RateValue> dateRates) {
		localProvider.setRates(baseCurrency, currency, dateRates);
		updatableRemoteProvider.setRates(baseCurrency, currency, dateRates);
	}
}
