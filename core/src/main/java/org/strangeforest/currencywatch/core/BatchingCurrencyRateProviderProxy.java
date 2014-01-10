package org.strangeforest.currencywatch.core;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.slf4j.*;
import org.strangeforest.util.*;

public class BatchingCurrencyRateProviderProxy extends ObservableCurrencyRateProviderProxy {

	private int batchSize;
	private int retryCount = RETRY_COUNT;

	private static final int BATCH_SIZE = 50;
	private static final int RETRY_COUNT = 20;

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchingCurrencyRateProviderProxy.class);

	public BatchingCurrencyRateProviderProxy(CurrencyRateProvider provider) {
		this(provider, BATCH_SIZE);
	}

	public BatchingCurrencyRateProviderProxy(CurrencyRateProvider provider, int batchSize) {
		super(provider);
		this.batchSize = batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	@Override public Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		AtomicInteger retriesLeft = new AtomicInteger(retryCount);
		if (dates.size() <= batchSize)
			return retryGetRates(baseCurrency, currency, dates, retriesLeft);
		else {
			Map<Date, RateValue> dateRates = new TreeMap<>();
			List<Date> batchDates = null;
			for (Date date : dates) {
				if (batchDates == null)
					batchDates = new ArrayList<>(batchSize);
				batchDates.add(date);
				if (batchDates.size() == batchSize) {
					dateRates.putAll(retryGetRates(baseCurrency, currency, batchDates, retriesLeft));
					batchDates = null;
				}
			}
			if (batchDates != null)
				dateRates.putAll(retryGetRates(baseCurrency, currency, batchDates, retriesLeft));
			return dateRates;
		}
	}

	private Map<Date, RateValue> retryGetRates(String baseCurrency, String currency, Collection<Date> batchDates, AtomicInteger retriesLeft) {
		while (true) {
			try {
				return super.getRates(baseCurrency, currency, batchDates);
			}
			catch (Exception ex) {
				String message = ExceptionUtil.getRootMessage(ex);
				if (retriesLeft.decrementAndGet() >= 0) {
					LOGGER.warn(String.format("Retrying request: getRate(%1$s, %2$s, Date[%3$d]): %4$s", baseCurrency, currency, batchDates.size(), message));
					notifyListeners("Retrying...");
				}
				else {
					notifyListeners(message);
					throw ex;
				}
			}
		}
	}
}