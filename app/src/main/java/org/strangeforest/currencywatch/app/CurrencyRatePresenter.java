package org.strangeforest.currencywatch.app;

import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.Timer;

import org.jfree.chart.*;
import org.slf4j.*;
import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;

import com.finsoft.util.*;

public class CurrencyRatePresenter implements AutoCloseable {

	private final CurrencyRateProvider provider;
	private final CurrencyChart chart;
	private final Collection<CurrencyRatePresenterListener> listeners = new CopyOnWriteArrayList<>();
	private ObservableCurrencyRateProvider remoteProvider;
	private CurrencyRateListener remoteProviderListener;
	private volatile CurrencyRate currencyRate;
	private volatile Thread dataThread;
	private volatile Timer speedTimer;
	private volatile boolean loading;
	private volatile int itemCount;
	private volatile int currItems;
	private volatile int currRemoteItems;
	private volatile long startTime;

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyRatePresenter.class);

	public CurrencyRatePresenter(CurrencyRateProvider provider) {
		super();
		this.provider = provider;
		chart = new CurrencyChart();
		setUpSpeedMeasuring();
	}

	private void setUpSpeedMeasuring() {
		if (provider instanceof ChainedCurrencyRateProvider) {
			CurrencyRateProvider aRemoteProvider = ((ChainedCurrencyRateProvider)provider).getRemoteProvider();
			if (aRemoteProvider instanceof ObservableCurrencyRateProvider)
				remoteProvider = (ObservableCurrencyRateProvider)aRemoteProvider;
		}
		if (remoteProvider != null) {
			remoteProviderListener = new CurrencyRateAdapter() {
				@Override public void newRate(CurrencyRateEvent rateEvent) {
					currRemoteItems++;
				}
			};
			remoteProvider.addListener(remoteProviderListener);
			speedTimer = new Timer(1000, new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					updateSpeed();
				}
			});
		}
		else
			LOGGER.warn("Data provider is not chained or remote provider is not observable: data fetching speed will not be available.");
	}

	private void cleanUpSpeedMeasuring() {
		stopSpeedUpdate();
		if (remoteProvider != null)
			remoteProvider.removeListener(remoteProviderListener);
	}

	public void addListener(CurrencyRatePresenterListener listener) {
		listeners.add(listener);
	}

	public void removeListener(CurrencyRatePresenterListener listener) {
		listeners.remove(listener);
	}

	public JFreeChart getChart() {
		return chart.getChart();
	}

	public void inputDataChanged(CurrencySymbol currency, Period period, SeriesQuality quality, boolean showBidAsk, boolean showMovAvg, boolean showBollBands, int movAvgPeriod) {
		if (currencyRate != null)
			currencyRate.close();
		chart.createSeries(currency, showBidAsk, showMovAvg, showBollBands);
		currencyRate = getCurrencyRate(currency.toString(), movAvgPeriod);
		applyPeriod(currencyRate, period.days(), quality.points());
	}

	private CurrencyRate getCurrencyRate(String currency, final int movAvgPeriod) {
		CurrencyRate currencyRate = new CurrencyRate(Util.BASE_CURRENCY, currency, provider);
		currencyRate.addListener(new CurrencyRateListener() {
			@Override public void newRate(final CurrencyRateEvent rateEvent) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						chart.updateBaseSeries(rateEvent);
						currItems++;
						updateProgress();
						setLoadingStatus();
						chart.updateDerivedSeries(movAvgPeriod);
					}
				});
			}
			@Override public void newRates(final CurrencyRateEvent[] rateEvents) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						for (CurrencyRateEvent rateEvent : rateEvents)
							chart.updateBaseSeries(rateEvent);
						currItems += rateEvents.length;
						updateProgress();
						setLoadingStatus();
						chart.updateDerivedSeries(movAvgPeriod);
					}
				});
			}
			@Override public void error(final String message) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						notifyStatusChanged(message, true);
					}
				});
			}
		});
		return currencyRate;
	}

	private void applyPeriod(final CurrencyRate rate, int days, int maxPoints) {
		if (dataThread != null)
			dataThread.interrupt();
		final DateRange dateRange = UIUtil.toDateRange(days);
		chart.setDateRange(dateRange);
		final int step = 1 + days/maxPoints;
		final Collection<Date> dates = dateRange.dates(step);
		itemCount = dates.size();
		currItems = 0;
		currRemoteItems = 0;
		startTime = System.currentTimeMillis();
		updateProgress();
		updateSpeed();
		startSpeedUpdate();
		dataThread = new Thread(new Runnable() {
			@Override public void run() {
				try {
					loading = true;
					try {
						notifyCurrentRate(rate.getRates(dateRange.dates(step*10))); // Fetch outline first
						notifyCurrentRate(rate.getRates(dates));
					}
					finally {
						loading = false;
						stopSpeedUpdate();
						updateSpeed();
						notifyStatusChanged(StringUtil.EMPTY, false);
					}
				}
				catch (Throwable th) {
					LOGGER.error("Error fetching data.", th);
				}
			}
		}, "Currency Data Fetcher");
		dataThread.start();
	}

	private void updateProgress() {
		notifyProgressChanged((100*currItems)/itemCount);
	}

	private void startSpeedUpdate() {
		if (speedTimer != null)
			speedTimer.start();
	}

	private void stopSpeedUpdate() {
		if (speedTimer != null)
			speedTimer.stop();
	}

	private void updateSpeed() {
		if (speedTimer != null) {
			long time = System.currentTimeMillis() - startTime;
			double itemsPerSec = time > 0L ? (1000.0*currRemoteItems)/time : 0.0;
			notifyRatesPerSecChanged(itemsPerSec);
		}
	}

	private void setLoadingStatus() {
		if (loading)
			notifyStatusChanged("Loading...", false);
	}

	private void notifyCurrentRate(Map<Date, RateValue> rates) {
		CurrentRate currentRate = CurrentRate.forRates(rates);
		if (currentRate != null) {
			for (CurrencyRatePresenterListener listener : listeners)
				listener.currentRate(currentRate);
		}
	}

	private void notifyStatusChanged(String status, boolean isError) {
		for (CurrencyRatePresenterListener listener : listeners)
			listener.statusChanged(status, isError);
	}

	private void notifyProgressChanged(int progress) {
		for (CurrencyRatePresenterListener listener : listeners)
			listener.progressChanged(progress);
	}

	private void notifyRatesPerSecChanged(double ratesPerSec) {
		for (CurrencyRatePresenterListener listener : listeners)
			listener.ratesPerSecChanged(ratesPerSec);
	}

	@Override public void close() {
		listeners.clear();
		cleanUpSpeedMeasuring();
		if (dataThread != null)
			dataThread.interrupt();
	}
}
