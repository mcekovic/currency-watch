package org.strangeforest.currencywatch.nbs;

public enum NBSFormat {

	CSV(1), ASCII(5);

	private int index;

	private NBSFormat(int index) {
		this.index = index;
	}

	public int index() {
		return index;
	}
}
