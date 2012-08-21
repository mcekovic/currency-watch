package test.strangeforest.currencywatch.unit.ui;

import java.util.*;

import org.junit.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;

import static org.junit.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrentRateTest {

	@Test
	public void currentRateIsDownwards() {
		CurrentRate currentRate = CurrentRate.forRates(RATES);
		assertEquals(RATES.lastKey(), currentRate.getDate());
		assertEquals(RATES.lastEntry().getValue(), currentRate.getRate());
		assertEquals(-1, currentRate.getDirection());
	}

	@Test
	public void currentRateIsUpwards() {
		SortedMap<Date,RateValue> rates = RATES.headMap(DATE5);
		CurrentRate currentRate = CurrentRate.forRates(rates);
		assertEquals(rates.lastKey(), currentRate.getDate());
		assertEquals(rates.get(rates.lastKey()), currentRate.getRate());
		assertEquals(1, currentRate.getDirection());
	}

	@Test
	public void currentRateIsStagnant() {
		CurrentRate currentRate = CurrentRate.forRates(RATES.tailMap(DATE5));
		assertEquals(DATE5, currentRate.getDate());
		assertEquals(RATES.get(DATE5), currentRate.getRate());
		assertEquals(0, currentRate.getDirection());
	}
}
