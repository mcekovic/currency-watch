package org.strangeforest.currencywatch.ui;

public abstract class DerivedPoint {

	private final long x;
	protected int count;

	protected DerivedPoint(long x) {
		super();
		this.x = x;
	}

	public long x() {
		return x;
	}

	public void add(double y) {
		count++;
	}
}
