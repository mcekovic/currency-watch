package test.strangeforest.currencywatch.unit.ui;

import org.jfree.data.time.*;
import org.junit.*;
import org.strangeforest.currencywatch.ui.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Offset.offset;
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
		assertThat(band[0]).isCloseTo(0.84, offset(0.01));
		assertThat(band[1]).isCloseTo(5.16, offset(0.01));
	}

	@Test
	public void pointsAreCalculatedOK() {
		TimeSeries lowBandSeries = new TimeSeries("TestBBLow");
		TimeSeries highBandSeries = new TimeSeries("TestBBHigh");

		new BollingerBandsPoints(2, 2.0).applyToSeries(TEST_SERIES, lowBandSeries, highBandSeries);

		assertThat(lowBandSeries.getValue(new Day(DATE1)).doubleValue()).isCloseTo(2.25, offset(0.01));
		assertThat(highBandSeries.getValue(new Day(DATE1)).doubleValue()).isCloseTo(3.25, offset(0.01));
		assertThat(lowBandSeries.getValue(new Day(DATE2)).doubleValue()).isCloseTo(1.92, offset(0.01));
		assertThat(highBandSeries.getValue(new Day(DATE2)).doubleValue()).isCloseTo(4.41, offset(0.01));
		assertThat(lowBandSeries.getValue(new Day(DATE3)).doubleValue()).isCloseTo(2.68, offset(0.01));
		assertThat(highBandSeries.getValue(new Day(DATE3)).doubleValue()).isCloseTo(4.32, offset(0.01));
		assertThat(lowBandSeries.getValue(new Day(DATE4)).doubleValue()).isCloseTo(3.18, offset(0.01));
		assertThat(highBandSeries.getValue(new Day(DATE4)).doubleValue()).isCloseTo(4.82, offset(0.01));
		assertThat(lowBandSeries.getValue(new Day(DATE5)).doubleValue()).isCloseTo(3.00, offset(0.01));
		assertThat(highBandSeries.getValue(new Day(DATE5)).doubleValue()).isCloseTo(5.00, offset(0.01));
	}
}
