package org.strangeforest.currencywatch.app;

public interface CurrencyRatePresenterListener {

	void statusChanged(String status, boolean isError);
	void progressChanged(int progress);
	void ratesPerSecChanged(double ratesPerSec);
}
