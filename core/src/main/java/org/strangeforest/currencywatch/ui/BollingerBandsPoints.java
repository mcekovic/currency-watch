package org.strangeforest.currencywatch.ui;

import org.jfree.data.time.*;

public class BollingerBandsPoints extends MovingAveragePoints {

	private final double factor;

	public BollingerBandsPoints(int period, double factor) {
		super(period);
		this.factor = factor;
	}

	@Override protected BollingerBandsPoint[] createPointArray(int count) {
		return new BollingerBandsPoint[count];
	}

	@Override protected BollingerBandsPoint createPoint(long x) {
		return new BollingerBandsPoint(x);
	}

	@Override protected void applyPointToSeriesForItem(MovingAveragePoint point, TimeSeries[] series, TimeSeriesDataItem seriesItem) {
		double[] bbPoints = ((BollingerBandsPoint)point).yBB(factor);
		series[0].addOrUpdate(seriesItem.getPeriod(), bbPoints[0]);
		series[1].addOrUpdate(seriesItem.getPeriod(), bbPoints[1]);
	}
}
