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
	}

	@Test(expected = IllegalStateException.class)
	public void unboundedDateRange() {
		DateRange dateRange = new DateRange(DATE, null);
		assertEquals(-1, dateRange.size());
		assertCollectionEquals(DATES, dateRange.dates());
	}

	private <E> void assertCollectionEquals(Collection<E> expected, Collection<E> actual) {
		assertEquals(new ArrayList<>(expected), new ArrayList<>(actual));
	}
}
