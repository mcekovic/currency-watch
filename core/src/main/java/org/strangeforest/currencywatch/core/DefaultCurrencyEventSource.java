package org.strangeforest.currencywatch.core;

import java.util.*;
import java.util.stream.*;

import org.joda.time.*;

public class DefaultCurrencyEventSource implements CurrencyEventSource {

	private final CurrencyEvent[] EVENTS = {
	   new CurrencyEvent("RSD", new LocalDate(2003, 12, 28).toDate(), "Elections in Serbia", "Coalition of DSS, G17+ and SPO-NS won elections."),
	   new CurrencyEvent("RSD", new LocalDate(2007, 1, 21).toDate(), "Elections in Serbia", "Coalition of DS, DSS-NS and G17+ won elections."),
	   new CurrencyEvent("RSD", new LocalDate(2008, 5, 11).toDate(), "Elections in Serbia", "Coalition of DS-G17+ and SPS won elections."),
	   new CurrencyEvent(null, new LocalDate(2008, 10, 1).toDate(), "2008 Financial crisis", "Global financial crisis in 2008."),
	   new CurrencyEvent("RSD", new LocalDate(2012, 5, 6).toDate(), "Elections in Serbia", "Coalition of SNS and SPS won elections."),
	   new CurrencyEvent("RSD", new LocalDate(2014, 3, 16).toDate(), "Elections in Serbia", "SNS won elections.")
	};

	@Override public Optional<CurrencyEvent> getEvent(final String currency, final Date date) {
		return Stream.of(EVENTS).filter(event -> event.isForCurrency(currency) && Objects.equals(event.getDate(), date)).findFirst();
	}

	@Override public Stream<CurrencyEvent> getEvents(final String currency, final Date fromDate, final Date toDate) {
		return Stream.of(EVENTS).filter(event -> event.isForCurrency(currency) && new DateRange(fromDate, toDate).contains(event.getDate()));
	}
}
