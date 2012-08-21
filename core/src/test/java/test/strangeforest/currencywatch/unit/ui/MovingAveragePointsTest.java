package test.strangeforest.currencywatch.unit.ui;

import org.jfree.data.time.*;
import org.junit.*;
import org.strangeforest.currencywatch.ui.*;

import static org.junit.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;
import static test.strangeforest.currencywatch.unit.ui.SeriesData.*;

public class MovingAveragePointsTest {

	@Test
	public void pointIsCalculatedOK() {
		MovingAveragePoint point = new MovingAveragePoint(1L);
		point.add(2.5);
		point.add(2.0);
		point.add(4.5);
		assertEquals(3.0, point.yAvg(), 0.0);
	}

	@Test
	public void pointsAreCalculatedOK() {
		TimeSeries movAvgSeries = new TimeSeries("TestMovAvg");

		new MovingAveragePoints(2).applyToSeries(TEST_SERIES, movAvgSeries);

		assertEquals(2.75, movAvgSeries.getValue(new Day(DATE1)).doubleValue(), 0.01);
		assertEquals(3.17, movAvgSeries.getValue(new Day(DATE2)).doubleValue(), 0.01);
		assertEquals(3.50, movAvgSeries.getValue(new Day(DATE3)).doubleValue(), 0.01);
		assertEquals(4.00, movAvgSeries.getValue(new Day(DATE4)).doubleValue(), 0.01);
		assertEquals(4.00, movAvgSeries.getValue(new Day(DATE5)).doubleValue(), 0.01);
	}
}
