package org.strangeforest.currencywatch.core;

public interface ObservableCurrencyRateProvider extends CurrencyRateProvider {

	void addListener(CurrencyRateListener listener);
	void removeListener(CurrencyRateListener listener);
}
