package org.strangeforest.currencywatch.ui;

public interface CurrencyRatePresenterListener {

	void statusChanged(String status, boolean isError);
	void progressChanged(int progress);
	void ratesPerSecChanged(double ratesPerSec);
}
