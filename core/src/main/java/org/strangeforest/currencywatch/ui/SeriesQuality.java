package org.strangeforest.currencywatch.ui;

public enum SeriesQuality {

	POOR("Poor", 50),
	LOW("Low", 100),
	NORMAL("Normal", 200),
	HIGH("High", 400),
	EXTRA("Extra", 1000);

	private final String label;
	private final int points;

	private SeriesQuality(String label, int points) {
		this.label = label;
		this.points = points;
	}

	public String label() {
		return label;
	}

	public int points() {
		return points;
	}

	@Override public String toString() {
		return label;
	}
}
