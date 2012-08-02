package org.strangeforest.currencywatch.core;

public abstract class CurrencyRateAdapter implements CurrencyRateListener {

	@Override public void newRate(CurrencyRateEvent rateEvent) {}

	@Override public void newRates(CurrencyRateEvent[] rateEvents) {
		for (CurrencyRateEvent rateEvent : rateEvents) {
			newRate(rateEvent);
		}
	}

	@Override public void error(String message) {}
}
