package org.strangeforest.currencywatch.ui;

import java.util.*;

public class BollingerBandsPoint extends MovingAveragePoint {

	private final List<Double> yList;

	public BollingerBandsPoint(long x) {
		super(x);
		yList = new ArrayList<>();
	}

	@Override public void add(double y) {
		super.add(y);
		yList.add(y);
	}

	public double[] yBB(double factor) {
		double yAvg = yAvg();
		double yStDev = yStDev();
		return new double[] {yAvg - factor*yStDev, yAvg + factor*yStDev};
	}

	private double yStDev() {
		double yAvg = yAvg();
		double ySqSum = 0.0;
		for (double y : yList) {
			double dy = y - yAvg;
			ySqSum += dy*dy;
		}
		return Math.sqrt(ySqSum/count);
	}
}
