package org.strangeforest.currencywatch.ui;

public interface CurrencyRatePresenterListener {

	void progressChanged(int progress);
	void ratesPerSecChanged(double ratesPerSec);
}
