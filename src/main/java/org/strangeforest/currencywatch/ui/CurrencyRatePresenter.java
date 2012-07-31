package org.strangeforest.currencywatch.ui;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

import org.jfree.chart.plot.*;
import org.jfree.data.time.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.core.DateRange;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;

import com.finsoft.util.*;

public class CurrencyRatePresenter implements AutoCloseable {

	private final CurrencyRateProvider provider;
	private final XYPlot chartPlot;
	private final CurrencyRatePresenterListener presenterListener;
	private CurrencyRate currencyRate;
	private Thread dataThread;
	private Timer speedTimer;
	private volatile int itemCount;
	private volatile int currItems;
	private volatile int currRemoteItems;
	private volatile long startTime;

	static final String[] CURRENCIES = {"EUR", "USD", "GBP", "CHF"};
	private static final Map<String, Integer> PERIOD_MAP = new LinkedHashMap<>();
	private static final Map<String, Integer> QUALITY_MAP = new LinkedHashMap<>();
	static final Integer[] MOV_AVG_PERIODS = {10, 20, 50, 100, 200, 500};
	static final String DEFAULT_CURRENCY = "EUR";
	static final String DEFAULT_PERIOD = "Month";
	static final String DEFAULT_QUALITY = "Normal";
	static final int DEFAULT_MOV_AVG_PERIOD = 20;

	private static final Date START_DATE = new GregorianCalendar(2002, 5, 15).getTime();
	private static final int REMOTE_PROVIDER_THREAD_COUNT = 10;

	static {
		PERIOD_MAP.put("Week", 7);
		PERIOD_MAP.put("2 Weeks", 14);
		PERIOD_MAP.put("Month", 30);
		PERIOD_MAP.put("Quarter", 91);
		PERIOD_MAP.put("2 Quarters", 182);
		PERIOD_MAP.put("Year", 365);
		PERIOD_MAP.put("2 Years", 730);
		PERIOD_MAP.put("5 Years", 1826);
//		PERIOD_MAP.put("10 Years", 3652);
		PERIOD_MAP.put("Maximum", DateUtil.dayDifference(START_DATE, getLastDate().getTime()));

		QUALITY_MAP.put("Minimum", 10);
		QUALITY_MAP.put("Poor", 25);
		QUALITY_MAP.put("Low", 50);
		QUALITY_MAP.put("Normal", 100);
		QUALITY_MAP.put("High", 200);
		QUALITY_MAP.put("Extra", 400);
		QUALITY_MAP.put("Maximum", 1000);
	}

	static String[] periods() {
		return PERIOD_MAP.keySet().toArray(new String[PERIOD_MAP.size()]);
	}

	static String[] qualities() {
		return QUALITY_MAP.keySet().toArray(new String[QUALITY_MAP.size()]);
	}

	public CurrencyRatePresenter(XYPlot chartPlot, CurrencyRatePresenterListener presenterListener) {
		super();
		this.chartPlot = chartPlot;
		this.presenterListener = presenterListener;
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider();
		remoteProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				currRemoteItems++;
				System.out.println(rateEvent);
			}
		});
		provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider("data/currency-rates.db4o"),
			new ParallelCurrencyRateProviderProxy(remoteProvider, REMOTE_PROVIDER_THREAD_COUNT)
		);
		provider.init();
		speedTimer = new Timer(1000, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				updateSpeedLabel();
			}
		});
	}

	public void inputDataChanged(String symbolTo, String period, String quality, boolean showBidAsk, boolean showMovAvg, boolean showBollBands, int movAvgPeriod) {
		TimeSeries currencySeries = new TimeSeries(symbolTo);
		TimeSeries bidSeries = null, askSeries = null;
		TimeSeries movAvgSeries = null;
		TimeSeries[] bollBandsSeries = null;
		TimeSeriesCollection dataSet = new TimeSeriesCollection(currencySeries);
		if (showBidAsk) {
			bidSeries = new TimeSeries(String.format("Bid(%s)", symbolTo));
			askSeries = new TimeSeries(String.format("Ask(%s)", symbolTo));
			dataSet.addSeries(bidSeries);
			dataSet.addSeries(askSeries);
		}
		if (showMovAvg) {
			movAvgSeries = new TimeSeries(String.format("MovAvg(%s)", symbolTo));
			dataSet.addSeries(movAvgSeries);
		}
		if (showBollBands) {
			TimeSeriesCollection bbDataSet = new TimeSeriesCollection(currencySeries);
			bollBandsSeries = new TimeSeries[] {
				new TimeSeries(String.format("BBLow(%s)", symbolTo)),
				new TimeSeries(String.format("BBHigh(%s)", symbolTo))
			};
			bbDataSet.addSeries(bollBandsSeries[0]);
			bbDataSet.addSeries(bollBandsSeries[1]);
			chartPlot.setDataset(1, bbDataSet);
		}
		else
			chartPlot.setDataset(1, null);
		CurrencyRate currencyRate = getCurrencyRate(symbolTo, currencySeries, bidSeries, askSeries, movAvgSeries, bollBandsSeries, movAvgPeriod);
		applyPeriod(currencyRate, currencySeries, PERIOD_MAP.get(period), QUALITY_MAP.get(quality));
		chartPlot.setDataset(0, dataSet);
	}

	private CurrencyRate getCurrencyRate(String symbolTo, final TimeSeries series, final TimeSeries bidSeries, final TimeSeries askSeries,
	                                     final TimeSeries movAvgSeries, final TimeSeries[] bollBandsSeries, final int movAvgPeriod) {
		if (currencyRate != null)
			currencyRate.close();
		currencyRate = new CurrencyRate("DIN", symbolTo, provider);
		currencyRate.addListener(new CurrencyRateListener() {
			@Override public void newRate(final CurrencyRateEvent rateEvent) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						updateBaseSeries(rateEvent);
						currItems++;
						updateProgressBar();
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
						updateProgressBar();
						updateDerivedSeries();
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
		Calendar cal = getLastDate();
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
		updateProgressBar();
		updateSpeedLabel();
		speedTimer.start();
		dataThread = new Thread(new Runnable() {
			@Override public void run() {
				rate.getRates(dateRange.dates(step*10)); // Fetch outline first 
				rate.getRates(dates);
				speedTimer.stop();
				updateSpeedLabel();
			}
		});
		dataThread.start();
	}

	private static Calendar getLastDate() {
		Calendar now = new GregorianCalendar();
		Calendar lastDate = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		if (now.get(Calendar.HOUR_OF_DAY) < 8)
			lastDate.add(Calendar.DATE, -1);
		return lastDate;
	}

	private void updateProgressBar() {
		presenterListener.progressChanged((100 * currItems) / itemCount);
	}

	private void updateSpeedLabel() {
		long time = System.currentTimeMillis() - startTime;
		double itemsPerSec = time > 0L ? (1000.0*currRemoteItems)/time : 0.0;
		presenterListener.ratesPerSecChanged(itemsPerSec);
	}

	@Override public void close() {
		if (provider != null)
			provider.close();
		if (dataThread != null)
			dataThread.interrupt();
		if (speedTimer != null)
			speedTimer.stop();
	}
}
