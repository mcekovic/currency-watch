package org.strangeforest.currencywatch.core;

import java.util.*;

import com.finsoft.util.*;

public class ObservableCurrencyRateProviderProxy extends BaseObservableCurrencyRateProvider implements Disposable {

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
			};
		}
		else
			providerListener = null;
	}

	public CurrencyRateProvider getProvider() {
		return provider;
	}

	@Override public void init() throws CurrencyRateException {
		provider.init();
	}

	@Override public void dispose() {
		unsubscribeFromProvider();
		provider.dispose();
	}

	@Override public RateValue getRate(String symbolFrom, String symbolTo, Date date) throws CurrencyRateException {
		RateValue rateValue = provider.getRate(symbolFrom, symbolTo, date);
		if (!isProviderObservable && hasAnyListener())
			notifyListeners(symbolFrom, symbolTo, date, rateValue);
		return rateValue;
	}

	@Override public Map<Date, RateValue> getRates(String symbolFrom, String symbolTo, Collection<Date> dates) throws CurrencyRateException {
		Map<Date, RateValue> dateRates = provider.getRates(symbolFrom, symbolTo, dates);
		if (!isProviderObservable && hasAnyListener())
			notifyListeners(symbolFrom, symbolTo, dateRates);
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