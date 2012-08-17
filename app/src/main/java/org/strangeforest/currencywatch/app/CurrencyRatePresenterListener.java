package org.strangeforest.currencywatch.app;

import org.strangeforest.currencywatch.ui.*;

public interface CurrencyRatePresenterListener {

	void currentRate(CurrentRate currentRate);
	void statusChanged(String status, boolean isError);
	void progressChanged(int progress);
	void ratesPerSecChanged(double ratesPerSec);
}
