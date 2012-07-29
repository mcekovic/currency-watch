package org.strangeforest.currencywatch.core;

import java.util.*;

public abstract class BaseObservableCurrencyRateProvider extends BaseCurrencyRateProvider implements ObservableCurrencyRateProvider {

	private final Collection<CurrencyRateListener> listeners = Collections.synchronizedSet(new HashSet<CurrencyRateListener>());

	@Override public void addListener(CurrencyRateListener listener) {
		listeners.add(listener);
	}

	@Override public void removeListener(CurrencyRateListener listener) {
		listeners.remove(listener);
	}

	protected boolean hasAnyListener() {
		return !listeners.isEmpty();
	}

	protected void notifyListeners(CurrencyRateEvent rateEvent) {
		for (CurrencyRateListener listener : listeners)
			listener.newRate(rateEvent);
	}

	protected void notifyListeners(CurrencyRateEvent[] rateEvents) {
		for (CurrencyRateListener listener : listeners)
			listener.newRates(rateEvents);
	}
}