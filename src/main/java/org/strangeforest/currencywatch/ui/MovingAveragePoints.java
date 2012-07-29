package org.strangeforest.currencywatch.ui;

import java.util.concurrent.*;

import org.jfree.data.time.*;

public class MovingAveragePoints extends DerivedPoints<MovingAveragePoint> {

	private final long halfPeriod;

	public MovingAveragePoints(int period) {
		super();
		halfPeriod = (period / 2) * TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
	}

	@Override public MovingAveragePoint[] createPointArray(int count) {
		return new MovingAveragePoint[count];
	}

	@Override public MovingAveragePoint createPoint(long x) {
		return new MovingAveragePoint(x);
	}

	@Override protected boolean isRelevantPoint(MovingAveragePoint point, long x) {
		long movAvgX = point.x();
		return x >= movAvgX - halfPeriod && x <= movAvgX + halfPeriod;
	}

	@Override protected void applyPointToSeriesForItem(MovingAveragePoint point, TimeSeries[] series, TimeSeriesDataItem seriesItem) {
		series[0].addOrUpdate(seriesItem.getPeriod(), point.yAvg());
	}
}
