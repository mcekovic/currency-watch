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

	protected final void notifyListeners(CurrencyRateEvent rateEvent) {
		for (CurrencyRateListener listener : listeners)
			listener.newRate(rateEvent);
	}

	protected final void notifyListeners(CurrencyRateEvent[] rateEvents) {
		for (CurrencyRateListener listener : listeners)
			listener.newRates(rateEvents);
	}

	protected final void notifyListeners(String symbolFrom, String symbolTo, Date date, RateValue rateValue) {
		notifyListeners(new CurrencyRateEvent(this, symbolFrom, symbolTo, date, rateValue));
	}

	protected final void notifyListeners(String symbolFrom, String symbolTo, Map<Date, RateValue> dateRates) {
		CurrencyRateEvent[] rateEvents = new CurrencyRateEvent[dateRates.size()];
		int i = 0;
		for (Map.Entry<Date, RateValue> dateRate : dateRates.entrySet())
			rateEvents[i++] = new CurrencyRateEvent(this, symbolFrom, symbolTo, dateRate.getKey(), dateRate.getValue());
		notifyListeners(rateEvents);
	}

	protected final void notifyListeners(String message) {
		for (CurrencyRateListener listener : listeners)
			listener.error(message);
	}
}