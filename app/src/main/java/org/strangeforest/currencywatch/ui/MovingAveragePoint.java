package org.strangeforest.currencywatch.ui;

public class MovingAveragePoint extends DerivedPoint {

	private double sum;
	private Double yAvg;

	public MovingAveragePoint(long x) {
		super(x);
	}

	@Override public void add(double y) {
		super.add(y);
		sum += y;
		yAvg = null;
	}

	public double yAvg() {
		if (yAvg == null)
			yAvg = count != 0 ? sum/count : 0.0;
		return yAvg;
	}
}
