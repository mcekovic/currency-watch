package test.strangeforest.currencywatch.unit.core;

import java.util.*;

import org.junit.*;
import org.strangeforest.currencywatch.core.*;

import static org.assertj.core.api.Assertions.*;
import static test.strangeforest.currencywatch.TestData.*;

public class DateRangeTest {

	@Test
	public void boundedDateRange() {
		DateRange dateRange = new DateRange(DATE1, DATE5);
		assertThat(dateRange).hasSize(5);
		assertThat(dateRange.dates()).containsExactlyElementsOf(DATES);
		assertThat(dateRange.dates(2)).containsExactly(DATE1, DATE3, DATE5);
		DateRange dateRange2 = new DateRange(DATE1, DATE4);
		assertThat(dateRange2.dates(2)).containsExactly(DATE1, DATE3, DATE4);
	}

	@Test(expected = IllegalStateException.class)
	public void unboundedDateRange() {
		DateRange dateRange = new DateRange(DATE, null);
		assertThat(dateRange.size()).isEqualTo(-1);
		assertThat(dateRange.dates()).containsExactlyElementsOf(DATES);
	}

	@Test
	public void dateRangeIterator() {
		Iterator<Date> dates = new DateRange(DATE1, DATE2).iterator();
		assertThat(dates.hasNext()).isTrue();
		assertThat(dates.next()).isEqualTo(DATE1);
		assertThat(dates.hasNext()).isTrue();
		assertThat(dates.next()).isEqualTo(DATE2);
		assertThat(dates.hasNext()).isFalse();
	}

	@Test(expected = NoSuchElementException.class)
	public void dateRangeIteratorFails() {
		Iterator<Date> dates = new DateRange(DATE1, DATE1).iterator();
		assertThat(dates.hasNext()).isTrue();
		assertThat(dates.next()).isEqualTo(DATE1);
		assertThat(dates.hasNext()).isFalse();
		assertThat(dates.next()).isEqualTo(DATE2);
	}
}
