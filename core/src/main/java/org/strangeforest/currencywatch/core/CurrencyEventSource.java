package org.strangeforest.currencywatch.core;

import java.util.*;

public interface CurrencyEventSource {

	CurrencyEvent getEvent(String currency, Date date);
	Iterable<CurrencyEvent> getEvents(String currency, Date fromDate, Date toDate);
}
