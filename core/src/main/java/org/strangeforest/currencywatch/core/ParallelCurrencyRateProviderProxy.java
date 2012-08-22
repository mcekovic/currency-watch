package org.strangeforest.currencywatch.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.slf4j.*;

import com.finsoft.concurrent.*;
import com.finsoft.util.*;

public class ParallelCurrencyRateProviderProxy extends ObservableCurrencyRateProviderProxy {

	private final ExecutorService executor;
	private int retryCount = RETRY_COUNT;
	private boolean resetProviderOnRetryFail = true;
	private int maxExceptions;

	private static final int RETRY_COUNT = 20;

	private static final Logger LOGGER = LoggerFactory.getLogger(ParallelCurrencyRateProviderProxy.class);

	public ParallelCurrencyRateProviderProxy(CurrencyRateProvider provider, int threadCount) {
		super(provider);
		executor = Executors.newFixedThreadPool(threadCount, new NamedThreadFactory("Currency Provider Worker"));
		maxExceptions = threadCount;
	}

	@Override public void close() {
		super.close();
		executor.shutdownNow();
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public void setResetProviderOnRetryFail(boolean resetProviderOnRetryFail) {
		this.resetProviderOnRetryFail = resetProviderOnRetryFail;
	}

	public void setMaxExceptions(int maxExceptions) {
		this.maxExceptions = maxExceptions;
	}

	@Override public Map<Date, RateValue> getRates(final String baseCurrency, final String currency, Collection<Date> dates) {
		Map<Date, RateValue> dateRates = new TreeMap<>();
		final AtomicInteger retriesLeft = new AtomicInteger(retryCount);
		final Queue<Future<DateRateValue>> results = new ArrayDeque<>();
		for (final Date date : dates) {
			Callable<DateRateValue> task = new Callable<DateRateValue>() {
				@Override public DateRateValue call() {
					try {
						RateValue rateValue = ParallelCurrencyRateProviderProxy.super.getRate(baseCurrency, currency, date);
						return new DateRateValue(date, rateValue);
					}
					catch (Exception ex) {
						String message = ExceptionUtil.getRootMessage(ex);
						if (retriesLeft.decrementAndGet() >= 0) {
							LOGGER.warn(String.format("Retrying request: getRate(%1$s, %2$s, %3$td-%3$tm-%3$tY): %4$s", baseCurrency, currency, date, message));
							notifyListeners("Retrying...");
							results.add(executor.submit(this));
							return null;
						}
						else {
							notifyListeners(message);
							if (resetProviderOnRetryFail) {
								LOGGER.error("Too many retry failures, resetting provider.");
								retriesLeft.addAndGet(retryCount);
								resetProvider();
							}
							throw ex;
						}
					}
				}
			};
			results.add(executor.submit(task));
		}
		int exCount = 0;
		while (!results.isEmpty() && !Thread.currentThread().isInterrupted()) {
			try {
				DateRateValue dateRate = results.remove().get();
				if (dateRate != null)
					dateRates.put(dateRate.date, dateRate.value);
			}
			catch (InterruptedException ignored) {
				break;
			}
			catch (ExecutionException ex) {
				Throwable cause = ex.getCause();
				cause = cause != null ? cause : ex;
				if (++exCount > maxExceptions) {
					for (Future<DateRateValue> result : results)
						result.cancel(false);
					throw CurrencyRateException.wrap(cause);
				}
				else
					LOGGER.warn("Error collecting currency rates.", cause);
			}
		}
		return dateRates;
	}

	private static class DateRateValue {

		public final Date date;
		public final RateValue value;

		public DateRateValue(Date date, RateValue value) {
			super();
			this.date = date;
			this.value = value;
		}
	}
}