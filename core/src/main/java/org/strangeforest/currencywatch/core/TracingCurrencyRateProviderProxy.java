package org.strangeforest.currencywatch.core;

import java.util.*;

import org.slf4j.*;

import static java.lang.String.*;

public class TracingCurrencyRateProviderProxy extends BaseCurrencyRateProvider {

	private final CurrencyRateProvider provider;
	private final Logger logger;

	public TracingCurrencyRateProviderProxy(CurrencyRateProvider provider) {
		this(provider, provider.getClass().getName());
	}

	public TracingCurrencyRateProviderProxy(CurrencyRateProvider provider, String loggerName) {
		super();
		this.provider = provider;
		logger = LoggerFactory.getLogger(loggerName);
	}

	@Override public void init() {
		provider.init();
	}

	@Override public void close() {
		provider.close();
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		if (logger.isTraceEnabled())
			logger.trace(format("getRate(baseCurrency=%1$s, currency=%2$s, date=%3$td-%3$tm-%3$tY)", baseCurrency, currency, date));
		RateValue rate = provider.getRate(baseCurrency, currency, date);
		if (logger.isTraceEnabled())
			logger.trace(format("~getRate: %1$s", rate));
		return rate;
	}

	@Override public Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		if (logger.isTraceEnabled())
			logger.trace(format("getRates(baseCurrency=%1$s, currency=%2$s, dates=Date[%3$d])", baseCurrency, currency, dates.size()));
		Map<Date, RateValue> rates = provider.getRates(baseCurrency, currency, dates);
		if (logger.isTraceEnabled())
			logger.trace(format("~getRates: Map<Date, RateValue>[%1$d]", rates.size()));
		return rates;
	}
}