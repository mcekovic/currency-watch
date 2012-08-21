package test.strangeforest.currencywatch.unit.ui;

import org.jfree.data.time.*;
import org.junit.*;
import org.strangeforest.currencywatch.ui.*;

import static org.junit.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;
import static test.strangeforest.currencywatch.unit.ui.SeriesData.*;

public class BollingerBandsPointsTest {

	@Test
	public void pointIsCalculatedOK() {
		BollingerBandsPoint point = new BollingerBandsPoint(1L);
		point.add(2.5);
		point.add(2.0);
		point.add(4.5);
		double[] band = point.yBB(2.0);
		assertEquals(0.84, band[0], 0.01);
		assertEquals(5.16, band[1], 0.01);
	}

	@Test
	public void pointsAreCalculatedOK() {
		TimeSeries lowBandSeries = new TimeSeries("TestBBLow");
		TimeSeries highBandSeries = new TimeSeries("TestBBHigh");

		new BollingerBandsPoints(2, 2.0).applyToSeries(TEST_SERIES, lowBandSeries, highBandSeries);

		assertEquals(2.25, lowBandSeries.getValue(new Day(DATE1)).doubleValue(), 0.01);
		assertEquals(3.25, highBandSeries.getValue(new Day(DATE1)).doubleValue(), 0.01);
		assertEquals(1.92, lowBandSeries.getValue(new Day(DATE2)).doubleValue(), 0.01);
		assertEquals(4.41, highBandSeries.getValue(new Day(DATE2)).doubleValue(), 0.01);
		assertEquals(2.68, lowBandSeries.getValue(new Day(DATE3)).doubleValue(), 0.01);
		assertEquals(4.32, highBandSeries.getValue(new Day(DATE3)).doubleValue(), 0.01);
		assertEquals(3.18, lowBandSeries.getValue(new Day(DATE4)).doubleValue(), 0.01);
		assertEquals(4.82, highBandSeries.getValue(new Day(DATE4)).doubleValue(), 0.01);
		assertEquals(3.00, lowBandSeries.getValue(new Day(DATE5)).doubleValue(), 0.01);
		assertEquals(5.00, highBandSeries.getValue(new Day(DATE5)).doubleValue(), 0.01);
	}
}
