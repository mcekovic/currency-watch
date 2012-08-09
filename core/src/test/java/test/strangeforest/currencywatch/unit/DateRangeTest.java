package test.strangeforest.currencywatch.unit;

import java.util.*;

import org.junit.*;
import org.strangeforest.currencywatch.core.*;

import static org.junit.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class DateRangeTest {

	@Test
	public void boundedDateRange() {
		DateRange dateRange = new DateRange(DATE1, DATE5);
		assertEquals(5, dateRange.size());
		assertCollectionEquals(DATES, dateRange.dates());
		assertCollectionEquals(Arrays.asList(DATE1, DATE3, DATE5), dateRange.dates(2));
		DateRange dateRange2 = new DateRange(DATE1, DATE4);
		assertCollectionEquals(Arrays.asList(DATE1, DATE3, DATE4), dateRange2.dates(2));
	}

	@Test(expected = IllegalStateException.class)
	public void unboundedDateRange() {
		DateRange dateRange = new DateRange(DATE, null);
		assertEquals(-1, dateRange.size());
		assertCollectionEquals(DATES, dateRange.dates());
	}

	@Test
	public void dateRangeIterator() {
		Iterator<Date> dates = new DateRange(DATE1, DATE2).iterator();
		assertTrue(dates.hasNext());
		assertEquals(DATE1, dates.next());
		assertTrue(dates.hasNext());
		assertEquals(DATE2, dates.next());
		assertFalse(dates.hasNext());
	}

	@Test(expected = NoSuchElementException.class)
	public void dateRangeIteratorFails() {
		Iterator<Date> dates = new DateRange(DATE1, DATE1).iterator();
		assertTrue(dates.hasNext());
		assertEquals(DATE1, dates.next());
		assertFalse(dates.hasNext());
		assertEquals(DATE2, dates.next());
	}

	private <E> void assertCollectionEquals(Collection<E> expected, Collection<E> actual) {
		assertEquals(new ArrayList<>(expected), new ArrayList<>(actual));
	}
}
