package org.strangeforest.currencywatch.web;

import javax.annotation.*;
import javax.faces.bean.*;

import org.slf4j.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;

@ManagedBean(name = "chartApp")
@ApplicationScoped
public class ChartAppBean {

	private CurrencyRateProvider provider;

	private static final String DB4O_DATA_FILE = "/.currency-watch/data/currency-rates.db4o";
	private static final int REMOTE_PROVIDER_THREAD_COUNT = 10;

	private static final Logger LOGGER = LoggerFactory.getLogger(ChartAppBean.class);

	@PostConstruct
	public void setUp() {
		provider = createProvider();
	}

	@PreDestroy
	public void cleanUp() {
		if (provider != null)
			provider.close();
	}

	public CurrencyRateProvider getProvider() {
		return provider;
	}

	private static CurrencyRateProvider createProvider() {
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider();
		remoteProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug(rateEvent.toString());
			}
		});
		CurrencyRateProvider provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider(getDb4oDataFile()),
			new ParallelCurrencyRateProviderProxy(remoteProvider, REMOTE_PROVIDER_THREAD_COUNT)
		);
		provider.init();
		return provider;
	}

	private static String getDb4oDataFile() {
		return System.getProperty("user.home") + DB4O_DATA_FILE;
	}
}
