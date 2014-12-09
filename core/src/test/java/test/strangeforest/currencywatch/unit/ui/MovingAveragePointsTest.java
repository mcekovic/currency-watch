package test.strangeforest.currencywatch.unit.ui;

import org.jfree.data.time.*;
import org.junit.*;
import org.strangeforest.currencywatch.ui.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Offset.offset;
import static test.strangeforest.currencywatch.TestData.*;
import static test.strangeforest.currencywatch.unit.ui.SeriesData.*;

public class MovingAveragePointsTest {

	@Test
	public void pointIsCalculatedOK() {
		MovingAveragePoint point = new MovingAveragePoint(1L);
		point.add(2.5);
		point.add(2.0);
		point.add(4.5);
		assertThat(point.yAvg()).isEqualTo(3.0);
	}

	@Test
	public void pointsAreCalculatedOK() {
		TimeSeries movAvgSeries = new TimeSeries("TestMovAvg");

		new MovingAveragePoints(2).applyToSeries(TEST_SERIES, movAvgSeries);

		assertThat(movAvgSeries.getValue(new Day(DATE1)).doubleValue()).isCloseTo(2.75, offset(0.01));
		assertThat(movAvgSeries.getValue(new Day(DATE2)).doubleValue()).isCloseTo(3.17, offset(0.01));
		assertThat(movAvgSeries.getValue(new Day(DATE3)).doubleValue()).isCloseTo(3.50, offset(0.01));
		assertThat(movAvgSeries.getValue(new Day(DATE4)).doubleValue()).isCloseTo(4.00, offset(0.01));
		assertThat(movAvgSeries.getValue(new Day(DATE5)).doubleValue()).isCloseTo(4.00, offset(0.01));
	}
}
