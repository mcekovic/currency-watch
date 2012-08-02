package org.strangeforest.currencywatch.ui;

public enum SeriesQuality {

	MINIMUM("Minimum", 10),
	POOR("Poor", 25),
	LOW("Low", 50),
	NORMAL("Normal", 100),
	HIGH("High", 200),
	EXTRA("Extra", 400),
	MAXIMUM("Maximum", 1000);

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
