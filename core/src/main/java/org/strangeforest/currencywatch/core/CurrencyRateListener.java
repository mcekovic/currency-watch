package org.strangeforest.currencywatch.core;

import java.util.*;

public interface CurrencyRateListener extends EventListener {

	void newRate(CurrencyRateEvent rateEvent);

	default void newRates(CurrencyRateEvent[] rateEvents) {
		for (CurrencyRateEvent rateEvent : rateEvents)
			newRate(rateEvent);
	}

	default void error(String message) {}
}