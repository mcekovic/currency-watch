package org.strangeforest.currencywatch.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ParallelCurrencyRateProviderProxy extends ObservableCurrencyRateProviderProxy {

	private final ExecutorService executor;
	private int retryCount = RETRY_COUNT;
	private boolean resetProviderOnRetryFail = true;
	private int maxExceptions;

	private static final int RETRY_COUNT = 20;

	public ParallelCurrencyRateProviderProxy(CurrencyRateProvider provider, int threadCount) {
		super(provider);
		executor = Executors.newFixedThreadPool(threadCount);
		maxExceptions = threadCount;
	}

	@Override public void close() {
		super.close();
		executor.shutdownNow();
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public boolean isResetProviderOnRetryFail() {
		return resetProviderOnRetryFail;
	}

	public void setResetProviderOnRetryFail(boolean resetProviderOnRetryFail) {
		this.resetProviderOnRetryFail = resetProviderOnRetryFail;
	}

	public int getMaxExceptions() {
		return maxExceptions;
	}

	public void setMaxExceptions(int maxExceptions) {
		this.maxExceptions = maxExceptions;
	}

	@Override public Map<Date, RateValue> getRates(final String symbolFrom, final String symbolTo, Collection<Date> dates) {
		Map<Date, RateValue> dateValues = new TreeMap<>();
		final AtomicInteger retriesLeft = new AtomicInteger(retryCount);
		final Queue<Future<DateRateValue>> results = new ArrayDeque<>();
		for (final Date date : dates) {
			Callable<DateRateValue> task = new Callable<DateRateValue>() {
				@Override public DateRateValue call() throws Exception {
					try {
						RateValue rateValue = provider.getRate(symbolFrom, symbolTo, date);
						if (!isProviderObservable && hasAnyListener())
							notifyListeners(symbolFrom, symbolTo, date, rateValue);
						return new DateRateValue(date, rateValue);
					}
					catch (Exception ex) {
						int left = retriesLeft.decrementAndGet();
						if (left >= 0) {
							System.out.printf("Retrying request: getRate(%1$s, %2$s, %3$td-%3$tm-%3$tY): %4$s\n", symbolFrom, symbolTo, date, ex);
							results.add(executor.submit(this));
							return null;
						}
						else {
							if (resetProviderOnRetryFail) {
								System.out.println("Too many retry failures, resetting provider.");
								provider.close();
								provider.init();
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
				DateRateValue dateValue = results.remove().get();
				if (dateValue != null)
					dateValues.put(dateValue.date, dateValue.value);
			}
			catch (InterruptedException ignored) {
				break;
			}
			catch (ExecutionException ex) {
				Throwable cause = ex.getCause();
				cause = cause != null ? cause : ex;
				if (++exCount > maxExceptions)
					throw CurrencyRateException.wrap(cause);
				else
					cause.printStackTrace();
			}
		}
		return dateValues;
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