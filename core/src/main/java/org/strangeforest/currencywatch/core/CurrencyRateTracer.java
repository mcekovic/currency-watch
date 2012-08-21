package org.strangeforest.currencywatch.core;

import java.util.*;

import org.slf4j.*;

public class CurrencyRateTracer extends CurrencyRateAdapter {

	private final Logger logger;

	public CurrencyRateTracer() {
		super();
		logger = LoggerFactory.getLogger(CurrencyRateTracer.class);
	}

	public CurrencyRateTracer(String loggerName) {
		super();
		logger = LoggerFactory.getLogger(loggerName);
	}

	@Override public void newRate(CurrencyRateEvent rateEvent) {
		if (logger.isTraceEnabled())
			logger.trace(rateEvent.toString());
	}

	@Override public void newRates(CurrencyRateEvent[] rateEvents) {
		if (logger.isTraceEnabled())
			logger.trace(Arrays.toString(rateEvents));
	}
}