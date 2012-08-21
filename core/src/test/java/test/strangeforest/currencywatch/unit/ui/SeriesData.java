package test.strangeforest.currencywatch.unit.ui;

import org.jfree.data.time.*;

import static test.strangeforest.currencywatch.TestData.*;

public abstract class SeriesData {

	public static final TimeSeries TEST_SERIES = new TimeSeries("Test");

	static {
		TEST_SERIES.addOrUpdate(new Day(DATE1), 2.5);
		TEST_SERIES.addOrUpdate(new Day(DATE2), 3.0);
		TEST_SERIES.addOrUpdate(new Day(DATE3), 4.0);
		TEST_SERIES.addOrUpdate(new Day(DATE4), 3.5);
		TEST_SERIES.addOrUpdate(new Day(DATE5), 4.5);
	}
}
