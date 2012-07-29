package org.strangeforest.currencywatch.ui;

import java.util.*;

import org.jfree.data.time.*;

public abstract class DerivedPoints<P extends DerivedPoint> {

	protected abstract P[] createPointArray(int count);
	protected abstract P createPoint(long x);
	protected abstract boolean isRelevantPoint(P point, long x);
	protected abstract void applyPointToSeriesForItem(P point, TimeSeries[] series, TimeSeriesDataItem seriesItem);

	public void applyToSeries(TimeSeries sourceSeries, TimeSeries... series) {
		List<TimeSeriesDataItem> items = sourceSeries.getItems();
		int count = items.size();
		P[] points = createPointArray(count);
		for (int i = 0; i < count; i++)
			points[i] = createPoint(items.get(i).getPeriod().getFirstMillisecond());
		for (TimeSeriesDataItem item : items) {
			long x = item.getPeriod().getFirstMillisecond();
			for (P point : points) {
				if (isRelevantPoint(point, x))
					point.add(item.getValue().doubleValue());
			}
		}
		for (int i = 0; i < count; i++)
			applyPointToSeriesForItem(points[i], series, items.get(i));
	}
}
