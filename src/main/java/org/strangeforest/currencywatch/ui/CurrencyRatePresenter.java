package org.strangeforest.currencywatch.ui;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

import org.jfree.chart.*;
import org.jfree.data.time.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.core.DateRange;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;

import com.finsoft.util.*;

public class CurrencyRatePresenter implements Disposable {

	private CurrencyRateProvider provider;
	private ChartPanel chartPanel;
	private JProgressBar progressBar;
	private final JLabel speedLabel;
	private CurrencyRate currencyRate;
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

	public CurrencyRatePresenter(ChartPanel chartPanel, JProgressBar progressBar, JLabel speedLabel) {
		super();
		this.chartPanel = chartPanel;
		this.progressBar = progressBar;
		this.speedLabel = speedLabel;
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider();
		remoteProvider.addListener(new CurrencyRateListener() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				currRemoteItems++;
				System.out.println(rateEvent);
			}
		});
		provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider("data/currency-rates.db4o"),
			new ParallelCurrencyRateProviderProxy(remoteProvider, REMOTE_PROVIDER_THREAD_COUNT)
		);
		try {
			provider.init();
		}
		catch (CurrencyRateException ex) {
			ex.printStackTrace();
		}
		speedTimer = new Timer(1000, new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				updateSpeedLabel();
			}
		});
	}

	public void inputDataChanged(String symbolTo, String period, String quality, boolean showMovAvg, int movAvgPeriod) {
		TimeSeries currencySeries = new TimeSeries(symbolTo);
		TimeSeries movAvgSeries = null;
		TimeSeriesCollection dataSet = new TimeSeriesCollection(currencySeries);
		if (showMovAvg) {
			movAvgSeries = new TimeSeries(String.format("MovAvg(%s)", symbolTo));
			dataSet.addSeries(movAvgSeries);
		}
		CurrencyRate currencyRate = getCurrencyRate(symbolTo, currencySeries, movAvgSeries, movAvgPeriod);
		applyPeriod(currencyRate, currencySeries, PERIOD_MAP.get(period), QUALITY_MAP.get(quality));
		chartPanel.getChart().getXYPlot().setDataset(dataSet);
	}

	private CurrencyRate getCurrencyRate(String symbolTo, final TimeSeries series, final TimeSeries movAvgSeries, final int movAvgPeriod) {
		if (currencyRate != null)
			currencyRate.dispose();
		currencyRate = new CurrencyRate("DIN", symbolTo, provider);
		currencyRate.addListener(new CurrencyRateListener() {
			@Override public void newRate(final CurrencyRateEvent rateEvent) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override public void run() {
						series.addOrUpdate(new Day(rateEvent.getDate()), rateEvent.getRate().getMiddle());
						currItems++;
						updateProgressBar();
						if (movAvgSeries != null && currItems == itemCount)
							applyMovingAvgerage(series, movAvgSeries, movAvgPeriod);
					}
				});
			}
		});
		return currencyRate;
	}

	private void applyPeriod(final CurrencyRate rate, TimeSeries series, int days, int maxPoints) {
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
		new Thread(new Runnable() {
			@Override public void run() {
				rate.getRates(dateRange.dates(step*10)); // Fetch outline first 
				rate.getRates(dates);
				speedTimer.stop();
				updateSpeedLabel();
			}
		}).start();
	}

	private void applyMovingAvgerage(TimeSeries series, TimeSeries movAvgSeries, int movAvgPeriod) {
		long halfPeriod = (movAvgPeriod / 2) * DateUtil.MILLISECONDS_PER_DAY;
		List<TimeSeriesDataItem> items = series.getItems();
		int count = items.size();
		MovAvgItem[] movAvgs = new MovAvgItem[count];
		for (int i = 0; i < count; i++)
			movAvgs[i] = new MovAvgItem(items.get(i).getPeriod().getFirstMillisecond());
		for (TimeSeriesDataItem item : items) {
			long x = item.getPeriod().getFirstMillisecond();
			double y = item.getValue().doubleValue();
			for (MovAvgItem movAvg : movAvgs) {
				if (x >= movAvg.x - halfPeriod && x <= movAvg.x + halfPeriod) {
					movAvg.count++;
					movAvg.sum += y;
				}
			}
		}
		for (int i = 0; i < count; i++)
			movAvgSeries.addOrUpdate(items.get(i).getPeriod(), movAvgs[i].y());
	}

	private static Calendar getLastDate() {
		Calendar now = new GregorianCalendar();
		GregorianCalendar lastDate = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		if (now.get(Calendar.HOUR_OF_DAY) < 8)
			lastDate.add(Calendar.DATE, -1);
		return lastDate;
	}

	private static class MovAvgItem {

		public long x;
		public int count;
		public double sum;

		public MovAvgItem(long x) {
			this.x = x;
		}

		public double y() {
			return sum/count;
		}
	}

	private void updateProgressBar() {
		progressBar.setValue((100*currItems)/itemCount);
	}

	private void updateSpeedLabel() {
		long time = System.currentTimeMillis() - startTime;
		double speed = time > 0L ? (1000.0*currRemoteItems)/time : 0.0;
		speedLabel.setText(String.format("%8.1f", speed) + " rate/s");
	}

	@Override public void dispose() {
		provider.dispose();
	}
}
