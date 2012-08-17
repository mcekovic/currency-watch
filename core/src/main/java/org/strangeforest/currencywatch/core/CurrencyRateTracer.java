package org.strangeforest.currencywatch.core;

import java.util.*;

import org.slf4j.*;

public class CurrencyRateTracer extends CurrencyRateAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyRateTracer.class);

	@Override public void newRate(CurrencyRateEvent rateEvent) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace(rateEvent.toString());
	}

	@Override public void newRates(CurrencyRateEvent[] rateEvents) {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace(Arrays.toString(rateEvents));
	}
}
