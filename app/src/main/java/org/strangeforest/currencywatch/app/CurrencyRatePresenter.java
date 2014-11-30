package org.strangeforest.currencywatch.app;

import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.Timer;

import org.jfree.chart.*;
import org.jfree.chart.event.*;
import org.slf4j.*;
import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;
import org.strangeforest.util.*;

import static java.util.Arrays.*;

public class CurrencyRatePresenter implements AutoCloseable {

	private final CurrencyRateProvider provider;
	private final CurrencyEventSource eventSource;
	private final CurrencyChart chart;
	private final Collection<CurrencyRatePresenterListener> listeners = new CopyOnWriteArrayList<>();
	private ObservableCurrencyRateProvider remoteProvider;
	private CurrencyRateListener remoteProviderListener;
	private volatile CurrencyRates currencyRates;
	private volatile Thread dataThread;
	private volatile Timer speedTimer;
	private volatile boolean loading;
	private volatile int itemCount;
	private volatile int currItems;
	private volatile int currRemoteItems;
	private volatile long startTime;

	private volatile CurrencySymbol currency;
	private volatile Period period;
	private volatile SeriesQuality quality;
	private volatile int movAvgPeriod;
	private final AxisChangeListener axisChangeListener;

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyRatePresenter.class);

	public CurrencyRatePresenter(CurrencyRateProvider provider) {
		super();
		this.provider = provider;
		eventSource = new DefaultCurrencyEventSource();
		chart = new CurrencyChart();
		axisChangeListener = event -> {
			if (currencyRates != null)
				currencyRates.close();
			currencyRates = getCurrencyRates(currency.toString(), movAvgPeriod);
			applyPeriod(currencyRates, chart.getDateRange(), quality.points());
		};
		setUpSpeedMeasuring();
	}

	private void setUpSpeedMeasuring() {
		if (provider instanceof ChainedCurrencyRateProvider) {
			CurrencyRateProvider aRemoteProvider = ((ChainedCurrencyRateProvider)provider).getRemoteProvider();
			if (aRemoteProvider instanceof ObservableCurrencyRateProvider)
				remoteProvider = (ObservableCurrencyRateProvider)aRemoteProvider;
		}
		if (remoteProvider != null) {
			remoteProviderListener = rateEvent -> currRemoteItems++;
			remoteProvider.addListener(remoteProviderListener);
			speedTimer = new Timer(1000, event -> updateSpeed());
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
		chart.removeDomainAxisChangeListener(axisChangeListener);
		if (currencyRates != null)
			currencyRates.close();
		this.currency = currency;
		if (period != this.period) {
			this.period = period;
			chart.setAutoRange();
		}
		this.quality = quality;
		this.movAvgPeriod = movAvgPeriod;
		chart.createSeries(currency, showBidAsk, showMovAvg, showBollBands);
		DateRange dateRange = UIUtil.toDateRange(period.days());
		chart.setDateRange(dateRange);
		currencyRates = getCurrencyRates(currency.toString(), movAvgPeriod);
		applyPeriod(currencyRates, dateRange, quality.points());
		chart.addDomainAxisChangeListener(axisChangeListener);
	}

	private CurrencyRates getCurrencyRates(String currency, int movAvgPeriod) {
		CurrencyRates rates = new CurrencyRates(Util.BASE_CURRENCY, currency, provider);
		rates.addListener(new CurrencyRateListener() {
			@Override
			public void newRate(final CurrencyRateEvent rateEvent) {
				SwingUtilities.invokeLater(() -> {
					chart.updateBaseSeries(rateEvent);
					addAnnotation(rateEvent.getDate());
					currItems++;
					updateProgress();
					setLoadingStatus();
					chart.updateDerivedSeries(movAvgPeriod);
				});
			}

			@Override
			public void newRates(CurrencyRateEvent[] rateEvents) {
				SwingUtilities.invokeLater(() -> {
					for (CurrencyRateEvent rateEvent : rateEvents) {
						chart.updateBaseSeries(rateEvent);
						addAnnotation(rateEvent.getDate());
					}
					currItems += rateEvents.length;
					updateProgress();
					setLoadingStatus();
					chart.updateDerivedSeries(movAvgPeriod);
				});
			}

			@Override
			public void error(String message) {
				SwingUtilities.invokeLater(() -> notifyStatusChanged(message, true));
			}
		});
		return rates;
	}

	private void applyPeriod(CurrencyRates rates, DateRange dateRange, int maxPoints) {
		if (dataThread != null)
			dataThread.interrupt();
		final int step = 1 + dateRange.size()/maxPoints;
		final Collection<Date> dates = dateRange.dates(step);
		itemCount = dates.size();
		currItems = 0;
		currRemoteItems = 0;
		startTime = System.currentTimeMillis();
		chart.clearAnnotations();
		updateProgress();
		updateSpeed();
		startSpeedUpdate();
		dataThread = new Thread(() -> {
			try {
				loading = true;
				try {
					notifyForRates(rates.getRates(dateRange.dates(step * 10))); // Fetch outline first
					notifyForRates(rates.getRates(dates));
				}
				finally {
					loading = false;
					SwingUtilities.invokeAndWait(() -> {
						addAnnotationsForMissingDates(dateRange);
						notifyStatusChanged(StringUtil.EMPTY, false);
						stopSpeedUpdate();
						updateSpeed();
					});
				}
			}
			catch (Throwable th) {
				LOGGER.error("Error fetching data.", th);
			}
		}, "Currency Data Fetcher");
		dataThread.start();
	}

	public void waitForData() throws InterruptedException {
		dataThread.join();
	}

	private void addAnnotation(Date date) {
		addAnnotation(currencyRates.getBaseCurrency(), date);
		addAnnotation(currencyRates.getCurrency(), date);
	}

	private void addAnnotation(String currency, Date date) {
		Optional<CurrencyEvent> event = eventSource.getEvent(currency, date);
		if (event.isPresent())
			chart.addAnnotationIfDateExists(event.get());
	}

	private void addAnnotationsForMissingDates(DateRange dateRange) {
		chart.addAnnotationsForMissingDates(eventSource, asList(currencyRates.getBaseCurrency(), currencyRates.getCurrency()), dateRange);
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

	private void notifyForRates(Map<Date, RateValue> rates) {
		final CurrentRate currentRate = CurrentRate.forRates(rates);
		if (currentRate != null)
			SwingUtilities.invokeLater(() -> notifyCurrencyRate(currentRate));
	}

	private void notifyCurrencyRate(CurrentRate currentRate) {
		for (CurrencyRatePresenterListener listener : listeners)
			listener.currentRate(currentRate);
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
