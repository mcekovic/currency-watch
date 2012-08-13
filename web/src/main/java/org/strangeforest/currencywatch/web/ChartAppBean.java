package org.strangeforest.currencywatch.web;

import javax.faces.bean.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;

@ManagedBean(name = "chartApp")
@ApplicationScoped
public class ChartAppBean {

	private final CurrencyRateProvider provider;

	private static final String DB4O_DATA_FILE = "WEB-INF/data/currency-rates.db4o"; //TODO: use file in user profile
	private static final int REMOTE_PROVIDER_THREAD_COUNT = 10;

	public ChartAppBean() {
		super();
		provider = createProvider();
	}

	public CurrencyRateProvider getProvider() {
		return provider;
	}

	private static CurrencyRateProvider createProvider() {
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider();
		remoteProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				System.out.println(rateEvent);
			}
		});
		CurrencyRateProvider provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider(DB4O_DATA_FILE),
			new ParallelCurrencyRateProviderProxy(remoteProvider, REMOTE_PROVIDER_THREAD_COUNT)
		);
		provider.init();
		return provider;
	}
}
