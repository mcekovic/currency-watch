package org.strangeforest.currencywatch.ui;

import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;

import org.jfree.chart.*;
import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.event.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.core.DateRange;

public class CurrencyChart {

	private final JFreeChart chart;
	private TimeSeries middleSeries;
	private TimeSeries bidSeries;
	private TimeSeries askSeries;
	private TimeSeries movAvgSeries;
	private TimeSeries[] bollBandsSeries;

	private static final Color MIDDLE_COLOR = new Color(255, 0, 0);
	private static final Color BID_COLOR = new Color(255, 128, 128);
	private static final Color ASK_COLOR = new Color(255, 128, 128);
	private static final Color MOV_AVG_COLOR = new Color(0, 0, 255);
	private static final Color BOLL_BANDS_COLOR = new Color(160, 208, 255, 64);
	private static final Color BOLL_BANDS_2_COLOR = new Color(255, 255, 255, 255);
	private static final Color ANNOTATION_COLOR = new Color(32, 32, 64);

	private static final Stroke BID_ASK_STROKE = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f);
	private static final Stroke MOV_AVG_STOKE = new BasicStroke(2f);
	private static final Stroke ANNOTATION_STOKE = new BasicStroke(0.5f);

	private static final double BOLLINGER_BANDS_FACTOR = 2.0;

	public CurrencyChart() {
		super();
		chart = createChart();
	}

	public JFreeChart getChart() {
		return chart;
	}

	private static JFreeChart createChart() {
		DateAxis xAxis = new DateAxis("Date");
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);
		NumberAxis yAxis = new NumberAxis("Middle");
		yAxis.setAutoRangeIncludesZero(false);

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
			StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
			new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")
		));
		renderer.setSeriesPaint(0, MIDDLE_COLOR);

		XYAreaRenderer2 bbRenderer = new XYAreaRenderer2();
		bbRenderer.setSeriesPaint(0, BOLL_BANDS_2_COLOR);
		bbRenderer.setSeriesPaint(1, BOLL_BANDS_COLOR);
		bbRenderer.setOutline(false);

		XYPlot plot = new XYPlot(null, xAxis, yAxis, renderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setRenderer(1, bbRenderer);

		JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(SystemColor.control);
		return chart;
	}

	public void createSeries(CurrencySymbol currency, boolean showBidAsk, boolean showMovAvg, boolean showBollBands) {
		middleSeries = new TimeSeries(currency);
		TimeSeriesCollection dataSet = new TimeSeriesCollection(middleSeries);

		if (showBidAsk) {
			bidSeries = new TimeSeries(String.format("Bid(%s)", currency));
			askSeries = new TimeSeries(String.format("Ask(%s)", currency));
			dataSet.addSeries(bidSeries);
			dataSet.addSeries(askSeries);
		}
		else {
			bidSeries = null;
			askSeries = null;
		}

		if (showMovAvg) {
			movAvgSeries = new TimeSeries(String.format("MovAvg(%s)", currency));
			dataSet.addSeries(movAvgSeries);
		}
		else
			movAvgSeries = null;

		XYPlot plot = chart.getXYPlot();
		plot.setDataset(0, dataSet);

		if (showBollBands) {
			TimeSeriesCollection bbDataSet = new TimeSeriesCollection();
			bollBandsSeries = new TimeSeries[] {
				new TimeSeries(String.format("BBLow(%s)", currency)),
				new TimeSeries(String.format("BBHigh(%s)", currency))
			};
			bbDataSet.addSeries(bollBandsSeries[0]);
			bbDataSet.addSeries(bollBandsSeries[1]);
			plot.setDataset(1, bbDataSet);
		}
		else {
			bollBandsSeries = null;
			plot.setDataset(1, null);
		}

		updateSeriesStyle(showBidAsk, showMovAvg);
	}

	private void updateSeriesStyle(boolean showBidAsk, boolean showMovAvg) {
		if (showBidAsk || showMovAvg) {
			XYItemRenderer renderer = chart.getXYPlot().getRenderer();
			if (showBidAsk) {
				renderer.setSeriesPaint(1, BID_COLOR);
				renderer.setSeriesPaint(2, ASK_COLOR);
				renderer.setSeriesStroke(1, BID_ASK_STROKE);
				renderer.setSeriesStroke(2, BID_ASK_STROKE);
			}
			if (showMovAvg) {
				int movAvgIndex = showBidAsk ? 3 : 1;
				renderer.setSeriesPaint(movAvgIndex, MOV_AVG_COLOR);
				renderer.setSeriesStroke(movAvgIndex, MOV_AVG_STOKE);
			}
		}
	}

	public DateRange getDateRange() {
		DateAxis domainAxis = (DateAxis)chart.getXYPlot().getDomainAxis();
		Date fromDate = Util.extractDate(new Date(domainAxis.getMinimumDate().getTime() + Util.MILLISECONDS_PER_DAY / 2));
		Date toDate = Util.extractDate(new Date(domainAxis.getMaximumDate().getTime() - Util.MILLISECONDS_PER_DAY / 2));
		return Util.trimDateRange(new DateRange(fromDate, toDate));
	}

	public void setDateRange(DateRange dateRange) {
		middleSeries.addOrUpdate(new Day(dateRange.getFrom()), 0);
		middleSeries.addOrUpdate(new Day(dateRange.getTo()), 0);
	}

	public void setAutoRange() {
		XYPlot plot = chart.getXYPlot();
		plot.getDomainAxis().setAutoRange(true);
		plot.getRangeAxis().setAutoRange(true);
	}

	public void setAutoRangeRangeAxis() {
		chart.getXYPlot().getRangeAxis().setAutoRange(true);
	}

	public void addDomainAxisChangeListener(AxisChangeListener listener) {
		chart.getXYPlot().getDomainAxis().addChangeListener(listener);
	}

	public void removeDomainAxisChangeListener(AxisChangeListener listener) {
		chart.getXYPlot().getDomainAxis().removeChangeListener(listener);
	}

	public void updateBaseSeries(Map<Date, RateValue> rates) {
		for (Map.Entry<Date, RateValue> rateEntry : rates.entrySet())
			updateBaseSeries(rateEntry.getKey(), rateEntry.getValue());
	}

	public void updateBaseSeries(CurrencyRateEvent rateEvent) {
		updateBaseSeries(rateEvent.getDate(), rateEvent.getRate());
	}

	private void updateBaseSeries(Date date, RateValue rate) {
		Day day = new Day(date);
		middleSeries.addOrUpdate(day, rate.getMiddle());
		if (bidSeries != null)
			bidSeries.addOrUpdate(day, rate.getBid());
		if (askSeries != null)
			askSeries.addOrUpdate(day, rate.getAsk());
	}

	public void updateDerivedSeries(int movAvgPeriod) {
		if (movAvgSeries != null)
			new MovingAveragePoints(movAvgPeriod).applyToSeries(middleSeries, movAvgSeries);
		if (bollBandsSeries != null)
			new BollingerBandsPoints(movAvgPeriod, BOLLINGER_BANDS_FACTOR).applyToSeries(middleSeries, bollBandsSeries);
	}


	// Annotations

	public void addAnnotations(CurrencyEventSource eventSource, List<String> currencies, DateRange dateRange) {
		addAnnotations(eventSource, currencies, dateRange, false);
	}

	public void addAnnotationsForMissingDates(CurrencyEventSource eventSource, List<String> currencies, DateRange dateRange) {
		addAnnotations(eventSource, currencies, dateRange, true);
	}

	private void addAnnotations(CurrencyEventSource eventSource, List<String> currencies, DateRange dateRange, boolean onlyForMissingDates) {
		for (String currency : currencies)
			addAnnotations(eventSource, currency, dateRange, onlyForMissingDates);
	}

	private void addAnnotations(CurrencyEventSource eventSource, String currency, DateRange dateRange, boolean onlyForMissingDates) {
		eventSource.getEvents(currency, dateRange.getFrom(), dateRange.getTo()).forEach(event -> {
			if (onlyForMissingDates)
				addAnnotationIfDateDoesNotExist(event);
			else
				addAnnotation(event);
		});
	}

	public void addAnnotation(CurrencyEvent event) {
		TimeSeriesDataItem dataItem = middleSeries.getDataItem(toDay(event));
		if (dataItem != null)
			addAnnotationToItem(event, dataItem.getValue().doubleValue());
		else
			addAnnotationBetweenNearestItems(event);
	}

	public void addAnnotationIfDateExists(CurrencyEvent event) {
		TimeSeriesDataItem dataItem = middleSeries.getDataItem(toDay(event));
		if (dataItem != null)
			addAnnotationToItem(event, dataItem.getValue().doubleValue());
	}

	private void addAnnotationIfDateDoesNotExist(CurrencyEvent event) {
		TimeSeriesDataItem dataItem = middleSeries.getDataItem(toDay(event));
		if (dataItem == null)
			addAnnotationBetweenNearestItems(event);
	}

	public void clearAnnotations() {
		chart.getXYPlot().clearAnnotations();
	}

	private Day toDay(CurrencyEvent event) {
		return new Day(event.getDate());
	}

	private void addAnnotationToItem(CurrencyEvent event, double atValue) {
		XYPointerAnnotation point = new XYPointerAnnotation(event.getTitle(), event.getDate().getTime(), atValue, -Math.PI/2);
		point.setLabelOffset(8);
		point.setBaseRadius(24);
		point.setTipRadius(2);
		point.setPaint(ANNOTATION_COLOR);
		point.setArrowPaint(ANNOTATION_COLOR);
		point.setArrowStroke(ANNOTATION_STOKE);
		point.setToolTipText(event.getDescription());
		chart.getXYPlot().addAnnotation(point);
	}

	private void addAnnotationBetweenNearestItems(CurrencyEvent event) {
		Day day = toDay(event);
		DataItemWithOffset next = findNextDataItem(day, true);
		DataItemWithOffset prev = findNextDataItem(day, false);
		double atValue = weightedAverage(next.dataItem.getValue().doubleValue(), prev.dataItem.getValue().doubleValue(), prev.dayOffset, next.dayOffset);
		addAnnotationToItem(event, atValue);
	}

	private DataItemWithOffset findNextDataItem(RegularTimePeriod day, boolean forward) {
		for (int i = 1;; i++) {
			day = forward ? day.next() : day.previous();
			TimeSeriesDataItem dataItem = middleSeries.getDataItem(day);
			if (dataItem != null)
				return new DataItemWithOffset(dataItem, i);
		}
	}

	private double weightedAverage(double x, double y, double xWeight, double yWeight) {
		return (x*xWeight + y*yWeight)/(xWeight + yWeight);
	}

	private static class DataItemWithOffset {
		private TimeSeriesDataItem dataItem;
		private int dayOffset;

		private DataItemWithOffset(TimeSeriesDataItem dataItem, int dayOffset) {
			this.dataItem = dataItem;
			this.dayOffset = dayOffset;
		}
	}
}
