package org.strangeforest.currencywatch.core;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static org.strangeforest.currencywatch.Util.*;

public class DefaultCurrencyEventSource implements CurrencyEventSource {

	private final CurrencyEvent[] EVENTS = {
	   new CurrencyEvent("RSD", toDate(LocalDate.of(2003, 12, 28)), "Elections in Serbia", "Coalition of DSS, G17+ and SPO-NS won elections."),
	   new CurrencyEvent("RSD", toDate(LocalDate.of(2007, 1, 21)), "Elections in Serbia", "Coalition of DS, DSS-NS and G17+ won elections."),
	   new CurrencyEvent("RSD", toDate(LocalDate.of(2008, 5, 11)), "Elections in Serbia", "Coalition of DS-G17+ and SPS won elections."),
	   new CurrencyEvent(null,  toDate(LocalDate.of(2008, 10, 1)), "2008 Financial crisis", "Global financial crisis in 2008."),
	   new CurrencyEvent("RSD", toDate(LocalDate.of(2012, 5, 6)), "Elections in Serbia", "Coalition of SNS and SPS won elections."),
	   new CurrencyEvent("RSD", toDate(LocalDate.of(2014, 3, 16)), "Elections in Serbia", "SNS won elections.")
	};

	@Override public Optional<CurrencyEvent> getEvent(final String currency, final Date date) {
		return Stream.of(EVENTS).filter(event -> event.isForCurrency(currency) && Objects.equals(event.getDate(), date)).findFirst();
	}

	@Override public Stream<CurrencyEvent> getEvents(final String currency, final Date fromDate, final Date toDate) {
		return Stream.of(EVENTS).filter(event -> event.isForCurrency(currency) && new DateRange(fromDate, toDate).contains(event.getDate()));
	}
}
