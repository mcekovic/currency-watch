package org.strangeforest.currencywatch.core;

import java.util.*;

import org.joda.time.*;
import org.strangeforest.util.*;

public class DefaultCurrencyEventSource implements CurrencyEventSource {

	private final List<CurrencyEvent> EVENTS = Arrays.asList(
      new CurrencyEvent("RSD", new LocalDate(2003, 12, 28).toDate(), "Elections in Serbia", "Coalition of DSS, G17+ and SPO-NS won elections."),
      new CurrencyEvent("RSD", new LocalDate(2007, 1, 21).toDate(), "Elections in Serbia", "Coalition of DS, DSS-NS and G17+ won elections."),
		new CurrencyEvent("RSD", new LocalDate(2008, 5, 11).toDate(), "Elections in Serbia", "Coalition of DS-G17+ and SPS won elections."),
		new CurrencyEvent("RSD", new LocalDate(2012, 5, 6).toDate(), "Elections in Serbia", "Coalition of SNS and SPS won elections.")
	);

	@Override public CurrencyEvent getEvent(final String currency, final Date date) {
		return Algorithms.find(EVENTS, new Predicate<CurrencyEvent>() {
			@Override public boolean test(CurrencyEvent event) {
				return Objects.equals(event.getCurrency(), currency) && Objects.equals(event.getDate(), date);
			}
		});
	}

	public Iterable<CurrencyEvent> getEvents(final String currency, final Date fromDate, final Date toDate) {
		return Algorithms.select(EVENTS, new Predicate<CurrencyEvent>() {
			@Override public boolean test(CurrencyEvent event) {
				return Objects.equals(event.getCurrency(), currency) && new DateRange(fromDate, toDate).contains(event.getDate());
			}
		});
	}
}
