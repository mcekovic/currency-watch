package test.strangeforest.currencywatch.unit.ui;

import java.util.*;

import org.junit.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;

import static org.assertj.core.api.Assertions.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrentRateTest {

	@Test
	public void currentRateIsDownwards() {
		CurrentRate currentRate = CurrentRate.forRates(RATES);

		assertThat(currentRate.getDate()).isEqualTo(RATES.lastKey());
		assertThat(currentRate.getRate()).isEqualTo(RATES.lastEntry().getValue());
		assertThat(currentRate.getDirection()).isEqualTo(-1);
	}

	@Test
	public void currentRateIsUpwards() {
		SortedMap<Date,RateValue> rates = RATES.headMap(DATE5);
		CurrentRate currentRate = CurrentRate.forRates(rates);
		assertThat(currentRate.getDate()).isEqualTo(rates.lastKey());
		assertThat(currentRate.getRate()).isEqualTo(rates.get(rates.lastKey()));
		assertThat(currentRate.getDirection()).isEqualTo(1);
	}

	@Test
	public void currentRateIsStagnant() {
		CurrentRate currentRate = CurrentRate.forRates(RATES.tailMap(DATE5));
		assertThat(currentRate.getDate()).isEqualTo(DATE5);
		assertThat(currentRate.getRate()).isEqualTo(RATES.get(DATE5));
		assertThat(currentRate.getDirection()).isEqualTo(0);
	}
}
