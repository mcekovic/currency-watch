package org.strangeforest.currencywatch.core;

import java.util.*;

public class ObservableCurrencyRateProviderProxy extends BaseObservableCurrencyRateProvider {

	protected final CurrencyRateProvider provider;
	protected final boolean isProviderObservable;
	private final CurrencyRateListener providerListener;

	public ObservableCurrencyRateProviderProxy(CurrencyRateProvider provider) {
		super();
		this.provider = provider;
		isProviderObservable = provider instanceof ObservableCurrencyRateProvider;
		if (isProviderObservable) {
			providerListener = new CurrencyRateListener() {
				@Override public void newRate(CurrencyRateEvent rateEvent) {
					notifyListeners(rateEvent);
				}
				@Override public void newRates(CurrencyRateEvent[] rateEvents) {
					notifyListeners(rateEvents);
				}
				@Override public void error(String message) {
					notifyListeners(message);
				}
			};
		}
		else
			providerListener = null;
	}

	public CurrencyRateProvider getProvider() {
		return provider;
	}

	@Override public void init() {
		provider.init();
	}

	@Override public void close() {
		unsubscribeFromProvider();
		provider.close();
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		RateValue rateValue = provider.getRate(baseCurrency, currency, date);
		if (!isProviderObservable && hasAnyListener())
			notifyListeners(baseCurrency, currency, date, rateValue);
		return rateValue;
	}

	@Override public Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		Map<Date, RateValue> dateRates = provider.getRates(baseCurrency, currency, dates);
		if (!isProviderObservable && hasAnyListener())
			notifyListeners(baseCurrency, currency, dateRates);
		return dateRates;
	}

	@Override public void addListener(CurrencyRateListener listener) {
		if (!hasAnyListener())
			subscribeToProvider();
		super.addListener(listener);
	}

	@Override public void removeListener(CurrencyRateListener listener) {
		super.removeListener(listener);
		if (!hasAnyListener())
			unsubscribeFromProvider();
	}

	private void subscribeToProvider() {
		if (isProviderObservable)
			((ObservableCurrencyRateProvider)provider).addListener(providerListener);
	}

	public void unsubscribeFromProvider() {
		if (isProviderObservable)
			((ObservableCurrencyRateProvider)provider).removeListener(providerListener);
	}
}