package org.strangeforest.currencywatch.core;

import java.util.*;
import java.util.stream.*;

public interface CurrencyEventSource {

	Optional<CurrencyEvent> getEvent(String currency, Date date);
	Stream<CurrencyEvent> getEvents(String currency, Date fromDate, Date toDate);
}
