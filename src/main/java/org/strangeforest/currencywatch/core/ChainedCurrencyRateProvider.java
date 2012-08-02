package org.strangeforest.currencywatch.core;

import java.util.*;

public class ChainedCurrencyRateProvider extends BaseObservableCurrencyRateProvider {

	private final UpdatableCurrencyRateProvider localProvider;
	private final CurrencyRateProvider remoteProvider;
	private final boolean isLocalProviderObservable;
	private final boolean isRemoteProviderObservable;
	private final CurrencyRateListener rateListener;

	public ChainedCurrencyRateProvider(UpdatableCurrencyRateProvider localProvider, CurrencyRateProvider remoteProvider) {
		super();
		this.localProvider = localProvider;
		this.remoteProvider = remoteProvider;

		isLocalProviderObservable = localProvider instanceof ObservableCurrencyRateProvider;
		isRemoteProviderObservable = remoteProvider instanceof ObservableCurrencyRateProvider;
		if (isLocalProviderObservable || isRemoteProviderObservable) {
			rateListener = new CurrencyRateListener() {
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
			rateListener = null;
	}

	public UpdatableCurrencyRateProvider getLocalProvider() {
		return localProvider;
	}

	public CurrencyRateProvider getRemoteProvider() {
		return remoteProvider;
	}

	@Override public void init() {
		localProvider.init();
		remoteProvider.init();
	}

	@Override public void close() {
		unsubscribeFromLocalProvider();
		localProvider.close();
		unsubscribeFromRemoteProvider();
		remoteProvider.close();
	}

	@Override public RateValue getRate(String symbolFrom, String symbolTo, Date date) {
		RateValue rateValue = localProvider.getRate(symbolFrom, symbolTo, date);
		if (rateValue == null) {
			rateValue = remoteProvider.getRate(symbolFrom, symbolTo, date);
			if (rateValue != null)
				localProvider.setRate(symbolFrom, symbolTo, date, rateValue);
				if (hasAnyListener() && !isRemoteProviderObservable)
					notifyListeners(symbolFrom, symbolTo, date, rateValue);
		}
		else if (hasAnyListener() && !isLocalProviderObservable)
			notifyListeners(symbolFrom, symbolTo, date, rateValue);
		return rateValue;
	}

	@Override public Map<Date, RateValue> getRates(String symbolFrom, String symbolTo, Collection<Date> dates) {
		Map<Date, RateValue> dateRates = localProvider.getRates(symbolFrom, symbolTo, dates);
		Collection<Date> missingDates = new HashSet<>(dates);
		if (!dateRates.isEmpty()) {
			if (hasAnyListener() && !isLocalProviderObservable)
				notifyListeners(symbolFrom, symbolTo, dateRates);
			missingDates.removeAll(dateRates.keySet());
		}
		if (!missingDates.isEmpty()) {
			Map<Date, RateValue> newDateRates = remoteProvider.getRates(symbolFrom, symbolTo, missingDates);
			if (!newDateRates.isEmpty()) {
				if (hasAnyListener() && !isRemoteProviderObservable)
					notifyListeners(symbolFrom, symbolTo, newDateRates);
				localProvider.setRates(symbolFrom, symbolTo, newDateRates);
				dateRates = new HashMap<>(dateRates);
				dateRates.putAll(newDateRates);
			}
		}
		return dateRates;
	}

	@Override public void addListener(CurrencyRateListener listener) {
		if (!hasAnyListener()) {
			subscribeToLocalProvider();
			subscribeToRemoteProvider();
		}
		super.addListener(listener);
	}

	private void subscribeToLocalProvider() {
		if (isLocalProviderObservable)
			((ObservableCurrencyRateProvider)localProvider).addListener(rateListener);
	}

	private void subscribeToRemoteProvider() {
		if (isRemoteProviderObservable)
			((ObservableCurrencyRateProvider)remoteProvider).addListener(rateListener);
	}

	@Override public void removeListener(CurrencyRateListener listener) {
		super.removeListener(listener);
		if (!hasAnyListener()) {
			unsubscribeFromLocalProvider();
			unsubscribeFromRemoteProvider();
		}
	}

	private void unsubscribeFromLocalProvider() {
		if (isLocalProviderObservable)
			((ObservableCurrencyRateProvider)localProvider).removeListener(rateListener);
	}

	private void unsubscribeFromRemoteProvider() {
		if (isRemoteProviderObservable)
			((ObservableCurrencyRateProvider)remoteProvider).removeListener(rateListener);
	}
}