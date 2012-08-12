package org.strangeforest.currencywatch.core;

import java.util.*;

import com.finsoft.concurrent.*;

public class CurrencyRate extends BaseCurrencyRate implements AutoCloseable {

	private final LockableMap<Date, RateValue> dateRates;
	private final CurrencyRateProvider provider;
	private final boolean isProviderObservable;
	private final CurrencyRateListener providerListener;
	private final Collection<CurrencyRateListener> listeners = Collections.synchronizedSet(new HashSet<CurrencyRateListener>());

	public CurrencyRate(String baseCurrency, String currency) {
		this(baseCurrency, currency, null);
	}

	public CurrencyRate(String baseCurrency, String currency, CurrencyRateProvider provider) {
		super(baseCurrency, currency);
		dateRates = new LockableHashMap<>();
		this.provider = provider;
		isProviderObservable = provider instanceof ObservableCurrencyRateProvider;
		if (isProviderObservable) {
			providerListener = new CurrencyRateListener() {
				@Override public void newRate(CurrencyRateEvent rateEvent) {
					if (putRate(rateEvent.getDate(), rateEvent.getRate()))
						notifyListeners(rateEvent);
				}
				@Override public void newRates(CurrencyRateEvent[] rateEvents) {
					int eventCount = rateEvents.length;
					List<CurrencyRateEvent> newRateEvents = new ArrayList<>(eventCount);
					for (CurrencyRateEvent rateEvent : rateEvents) {
						if (putRate(rateEvent.getDate(), rateEvent.getRate()))
							newRateEvents.add(rateEvent);
					}
					if (!newRateEvents.isEmpty())
						notifyListeners(newRateEvents.size() == eventCount ? rateEvents : newRateEvents.toArray(new CurrencyRateEvent[newRateEvents.size()]));
				}
				@Override public void error(String message) {
					notifyListeners(message);
				}
			};
			((ObservableCurrencyRateProvider)provider).addListener(providerListener);
		}
		else
			providerListener = null;
	}

	@Override public RateValue getRate(Date date) {
		dateRates.lock(date);
		try {
			RateValue rateValue = dateRates.get(date);
			if (rateValue == null && provider != null) {
				rateValue = provider.getRate(baseCurrency, currency, date);
				if (!isProviderObservable && rateValue != null) {
					RateValue oldRateValue = dateRates.put(date, rateValue);
					if (oldRateValue == null || !oldRateValue.equals(rateValue))
						notifyListeners(date, rateValue);
				}
			}
			return rateValue;
		}
		catch (CurrencyRateException ex) {
			return null;
		}
		finally {
			dateRates.unlock(date);
		}
	}

	@Override public Map<Date, RateValue> getRates() {
		return new TreeMap<>(dateRates);
	}

	@Override public Map<Date, RateValue> getRates(Iterable<Date> dates) {
		Map<Date, RateValue> resultRates = new TreeMap<>();
		List<Date> datesForFetch = new ArrayList<>();
		for (Date date : dates) {
			dateRates.lock(date);
			try {
				RateValue rateValue = dateRates.get(date);
				if (rateValue != null)
					resultRates.put(date, rateValue);
				else if (provider != null)
					datesForFetch.add(date);
			}
			finally {
				dateRates.unlock(date);
			}
		}
		if (provider != null && !datesForFetch.isEmpty()) {
			Map<Date, RateValue> fetchedRates = provider.getRates(baseCurrency, currency, datesForFetch);
			if (!isProviderObservable) {
				List<CurrencyRateEvent> newRateEvents = new ArrayList<>(fetchedRates.size());
				for (Map.Entry<Date, RateValue> dateRate : fetchedRates.entrySet()) {
					Date date = dateRate.getKey();
					RateValue rate = dateRate.getValue();
					if (putRate(date, rate))
						newRateEvents.add(new CurrencyRateEvent(this, baseCurrency, currency, date, rate));
				}
				if (!newRateEvents.isEmpty())
					notifyListeners(newRateEvents.toArray(new CurrencyRateEvent[newRateEvents.size()]));
			}
			resultRates.putAll(fetchedRates);
		}
		return resultRates;
	}

	private boolean putRate(Date date, RateValue rateValue) {
		if (rateValue != null) {
			RateValue oldRateValue = dateRates.lockedPut(date, rateValue);
			return oldRateValue == null || !oldRateValue.equals(rateValue);
		}
		else
			return false;
	}

	public void addListener(CurrencyRateListener listener) {
		listeners.add(listener);
	}

	public void removeListener(CurrencyRateListener listener) {
		listeners.remove(listener);
	}

	private void notifyListeners(CurrencyRateEvent rateEvent) {
		for (CurrencyRateListener listener : listeners)
			listener.newRate(rateEvent);
	}

	private void notifyListeners(CurrencyRateEvent[] rateEvents) {
		for (CurrencyRateListener listener : listeners)
			listener.newRates(rateEvents);
	}

	private void notifyListeners(Date date, RateValue rate) {
		if (!listeners.isEmpty()) {
			CurrencyRateEvent event = new CurrencyRateEvent(this, baseCurrency, currency, date, rate);
			for (CurrencyRateListener listener : listeners)
				listener.newRate(event);
		}
	}

	private void notifyListeners(String message) {
		for (CurrencyRateListener listener : listeners)
			listener.error(message);
	}

	@Override public void close() {
		listeners.clear();
		if (isProviderObservable)
			((ObservableCurrencyRateProvider)provider).removeListener(providerListener);
	}
}