package org.strangeforest.currencywatch.app;

import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.Timer;

import org.jfree.chart.*;
import org.jfree.data.time.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.core.DateRange;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;
import org.strangeforest.currencywatch.ui.*;

import com.finsoft.util.*;

public class CurrencyRatePresenter implements AutoCloseable {

	private final CurrencyRateProvider provider;
	private final CurrencyChart chart;
	private final Collection<CurrencyRatePresenterListener> listeners;
	private volatile CurrencyRate currencyRate;
	private volatile Thread dataThread;
	private volatile Timer speedTimer;
	private volatile boolean loading;
	private volatile int itemCount;
	private volatile int currItems;
	private volatile int currRemoteItems;
	private volatile long startTime;

	private static final int REMOTE_PROVIDER_THREAD_COUNT = 10;

	public CurrencyRatePresenter() {
		super();
		provider = createProvider();
		chart = new CurrencyChart();
		listeners = new CopyOnWriteArrayList<>();
		speedTimer = new Timer(1000, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				updateSpeed();
			}
		});
	}

	private CurrencyRateProvider createProvider() {
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider();
		remoteProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				currRemoteItems++;
				System.out.println(rateEvent);
			}
		});
		CurrencyRateProvider provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider("data/currency-rates.db4o"),
			new ParallelCurrencyRateProviderProxy(remoteProvider, REMOTE_PROVIDER_THREAD_COUNT)
		);
		provider.init();
		return provider;
	}

	public JFreeChart getChart() {
		return chart.getChart();
	}

	public void addListener(CurrencyRatePresenterListener listener) {
		listeners.add(listener);
	}

	public void removeListener(CurrencyRatePresenterListener listener) {
		listeners.remove(listener);
	}

	public void inputDataChanged(CurrencySymbol currency, Period period, SeriesQuality quality, boolean showBidAsk, boolean showMovAvg, boolean showBollBands, int movAvgPeriod) {
		chart.updateSeriesStyle(showBidAsk, showMovAvg);

		TimeSeries currencySeries = new TimeSeries(currency);
		TimeSeries bidSeries = null, askSeries = null;
		TimeSeries movAvgSeries = null;
		TimeSeries[] bollBandsSeries = null;
		TimeSeriesCollection dataSet = new TimeSeriesCollection(currencySeries);
		if (showBidAsk) {
			bidSeries = new TimeSeries(String.format("Bid(%s)", currency));
			askSeries = new TimeSeries(String.format("Ask(%s)", currency));
			dataSet.addSeries(bidSeries);
			dataSet.addSeries(askSeries);
		}
		if (showMovAvg) {
			movAvgSeries = new TimeSeries(String.format("MovAvg(%s)", currency));
			dataSet.addSeries(movAvgSeries);
		}
		chart.setDataset(0, dataSet);
		if (showBollBands) {
			TimeSeriesCollection bbDataSet = new TimeSeriesCollection(currencySeries);
			bollBandsSeries = new TimeSeries[] {
				new TimeSeries(String.format("BBLow(%s)", currency)),
				new TimeSeries(String.format("BBHigh(%s)", currency))
			};
			bbDataSet.addSeries(bollBandsSeries[0]);
			bbDataSet.addSeries(bollBandsSeries[1]);
			chart.setDataset(1, bbDataSet);
		}
		else
			chart.setDataset(1, null);
		CurrencyRate currencyRate = getCurrencyRate(currency.toString(), currencySeries, bidSeries, askSeries, movAvgSeries, bollBandsSeries, movAvgPeriod);
		applyPeriod(currencyRate, currencySeries, period.days(), quality.points());
	}

	private CurrencyRate getCurrencyRate(String currency, final TimeSeries series, final TimeSeries bidSeries, final TimeSeries askSeries,
	                                     final TimeSeries movAvgSeries, final TimeSeries[] bollBandsSeries, final int movAvgPeriod) {
		if (currencyRate != null)
			currencyRate.close();
		currencyRate = new CurrencyRate(UIUtil.BASE_CURRENCY, currency, provider);
		currencyRate.addListener(new CurrencyRateListener() {
			@Override public void newRate(final CurrencyRateEvent rateEvent) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						updateBaseSeries(rateEvent);
						currItems++;
						updateProgress();
						setLoadingStatus();
						updateDerivedSeries();
					}
				});
			}
			@Override public void newRates(final CurrencyRateEvent[] rateEvents) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						for (CurrencyRateEvent rateEvent : rateEvents)
							updateBaseSeries(rateEvent);
						currItems += rateEvents.length;
						updateProgress();
						setLoadingStatus();
						updateDerivedSeries();
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

			private void updateBaseSeries(CurrencyRateEvent rateEvent) {
				Day day = new Day(rateEvent.getDate());
				RateValue rate = rateEvent.getRate();
				series.addOrUpdate(day, rate.getMiddle());
				if (bidSeries != null)
					bidSeries.addOrUpdate(day, rate.getBid());
				if (askSeries != null)
					askSeries.addOrUpdate(day, rate.getAsk());
			}

			private void updateDerivedSeries() {
				if (movAvgSeries != null)
					new MovingAveragePoints(movAvgPeriod).applyToSeries(series, movAvgSeries);
				if (bollBandsSeries != null)
					new BollingerBandsPoints(movAvgPeriod, 2.0).applyToSeries(series, bollBandsSeries);
			}
		});
		return currencyRate;
	}

	private void applyPeriod(final CurrencyRate rate, TimeSeries series, int days, int maxPoints) {
		if (dataThread != null)
			dataThread.interrupt();
		series.clear();
		Calendar cal = UIUtil.getLastDate();
		Date toDate = cal.getTime();
		cal.add(Calendar.DATE, -days);
		Date fromDate = cal.getTime();
		final DateRange dateRange = new DateRange(fromDate, toDate);
		final int step = 1 + days/maxPoints;
		final Collection<Date> dates = dateRange.dates(step);
		itemCount = dates.size();
		series.addOrUpdate(new Day(fromDate), 0);
		series.addOrUpdate(new Day(toDate), 0);
		currItems = 0;
		currRemoteItems = 0;
		startTime = System.currentTimeMillis();
		updateProgress();
		updateSpeed();
		speedTimer.start();
		dataThread = new Thread(new Runnable() {
			@Override public void run() {
				loading = true;
				try {
					rate.getRates(dateRange.dates(step*10)); // Fetch outline first
					rate.getRates(dates);
				}
				finally {
					loading = false;
					speedTimer.stop();
					updateSpeed();
					notifyStatusChanged(StringUtil.EMPTY, false);
				}
			}
		}, "Currency Data Fetcher");
		dataThread.start();
	}

	private void updateProgress() {
		notifyProgressChanged((100 * currItems) / itemCount);
	}

	private void updateSpeed() {
		long time = System.currentTimeMillis() - startTime;
		double itemsPerSec = time > 0L ? (1000.0*currRemoteItems)/time : 0.0;
		notifyRatesPerSecChanged(itemsPerSec);
	}

	private void setLoadingStatus() {
		if (loading)
			notifyStatusChanged("Loading...", false);
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
		if (provider != null)
			provider.close();
		if (dataThread != null)
			dataThread.interrupt();
		if (speedTimer != null)
			speedTimer.stop();
	}
}
