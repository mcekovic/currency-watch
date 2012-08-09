package org.strangeforest.currencywatch.core;

import java.util.*;

public interface CurrencyRateListener extends EventListener {

	void newRate(CurrencyRateEvent rateEvent);
	void newRates(CurrencyRateEvent[] rateEvents);
	void error(String message);
}