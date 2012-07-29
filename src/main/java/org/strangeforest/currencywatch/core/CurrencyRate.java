package org.strangeforest.currencywatch.core;

import java.util.*;

import com.finsoft.concurrent.*;
import com.finsoft.util.*;

public class CurrencyRate extends BaseCurrencyRate implements Disposable {

	private final LockableMap<Date, RateValue> dateRates;
	private final CurrencyRateProvider provider;
	private final boolean isProviderObservable;
	private final CurrencyRateListener providerListener;
	private final Collection<CurrencyRateListener> listeners = Collections.synchronizedSet(new HashSet<CurrencyRateListener>());

	public CurrencyRate(String symbolFrom, String symbolTo) {
		this(symbolFrom, symbolTo, null);
	}

	public CurrencyRate(String symbolFrom, String symbolTo, CurrencyRateProvider provider) {
		super(symbolFrom, symbolTo);
		dateRates = new LockableHashMap<>();
		this.provider = provider;
		isProviderObservable = provider instanceof ObservableCurrencyRateProvider;
		if (isProviderObservable) {
			providerListener = new CurrencyRateListener() {
				@Override public void newRate(CurrencyRateEvent rateEvent) {
					RateValue rateValue = rateEvent.getRate();
					if (rateValue != null) {
						RateValue oldRateValue = dateRates.lockedPut(rateEvent.getDate(), rateValue);
						if (oldRateValue == null || !oldRateValue.equals(rateValue))
							notifyListeners(rateEvent);
					}
				}
				@Override public void newRates(CurrencyRateEvent[] rateEvents) {
					for (CurrencyRateEvent rateEvent : rateEvents) {
						RateValue rateValue = rateEvent.getRate();
						if (rateValue != null)
							dateRates.lockedPut(rateEvent.getDate(), rateValue);
					}
					notifyListeners(rateEvents);
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
				rateValue = provider.getRate(symbolFrom, symbolTo, date);
				if (!isProviderObservable)
					setRate(date, rateValue, false);
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
		if (provider != null) {
			try {
				Map<Date, RateValue> fetchedRates = provider.getRates(symbolFrom, symbolTo, datesForFetch);
				for (Map.Entry<Date, RateValue> dateRate : fetchedRates.entrySet()) {
					Date date = dateRate.getKey();
					RateValue rateValue = dateRate.getValue();
					if (!isProviderObservable)
						setRate(date, rateValue, true);
					resultRates.put(date, rateValue);
				}
			}
			catch (CurrencyRateException ex) {
				ex.printStackTrace();
			}
		}
		return resultRates;
	}

	private void setRate(Date date, RateValue rateValue, boolean locked) {
		if (rateValue != null) {
			RateValue oldRateValue = locked ? dateRates.lockedPut(date, rateValue) : dateRates.put(date, rateValue);
			if (oldRateValue == null || !oldRateValue.equals(rateValue))
				notifyListeners(date, rateValue);
		}
	}

	public void addListener(CurrencyRateListener listener) {
		listeners.add(listener);
	}

	public void removeListener(CurrencyRateListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners(CurrencyRateEvent rateEvent) {
		for (CurrencyRateListener listener : listeners)
			listener.newRate(rateEvent);
	}

	protected void notifyListeners(CurrencyRateEvent[] rateEvents) {
		for (CurrencyRateListener listener : listeners)
			listener.newRates(rateEvents);
	}

	private void notifyListeners(Date date, RateValue rate) {
		if (!listeners.isEmpty()) {
			CurrencyRateEvent event = new CurrencyRateEvent(this, symbolFrom, symbolTo, date, rate);
			for (CurrencyRateListener listener : listeners)
				listener.newRate(event);
		}
	}

	@Override public void dispose() {
		listeners.clear();
		if (isProviderObservable)
			((ObservableCurrencyRateProvider)provider).removeListener(providerListener);
	}
}