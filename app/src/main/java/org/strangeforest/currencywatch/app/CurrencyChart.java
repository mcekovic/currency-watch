package org.strangeforest.currencywatch.app;

import java.awt.*;
import java.text.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;

public class CurrencyChart {

	private final JFreeChart chart;

	private static final Color MIDDLE_COLOR = new Color(255, 0, 0);
	private static final Color BID_COLOR = new Color(255, 128, 128);
	private static final Color ASK_COLOR = new Color(255, 128, 128);
	private static final Color MOV_AVG_COLOR = new Color(0, 0, 255);
	private static final Color BOLL_BANDS_COLOR = new Color(192, 224, 255, 64);
	private static final Color BOLL_BANDS_2_COLOR = new Color(255, 255, 255, 255);
	private static final Color TRANSPARENT_PAINT = new Color(255, 255, 255, 0);

	private static final BasicStroke BID_ASK_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f);
	private static final BasicStroke MOV_AVG_STOKE = new BasicStroke(2);

	public CurrencyChart() {
		super();
		chart = createChart();
	}

	private static JFreeChart createChart() {
		DateAxis xAxis = new DateAxis("Date");
		NumberAxis yAxis = new NumberAxis("Middle");
		yAxis.setAutoRangeIncludesZero(false);

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
			StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
			new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")
		));
		renderer.setSeriesPaint(0, MIDDLE_COLOR);

		XYAreaRenderer2 bbRenderer = new XYAreaRenderer2();
		bbRenderer.setSeriesPaint(0, TRANSPARENT_PAINT);
		bbRenderer.setSeriesPaint(1, BOLL_BANDS_2_COLOR);
		bbRenderer.setSeriesPaint(2, BOLL_BANDS_COLOR);
		bbRenderer.setOutline(false);

		XYPlot plot = new XYPlot(null, xAxis, yAxis, renderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setRenderer(1, bbRenderer);

		JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(SystemColor.control);
		return chart;
	}

	public JFreeChart getChart() {
		return chart;
	}

	public void updateSeriesStyle(boolean showBidAsk, boolean showMovAvg) {
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

	public void setDataset(int index, XYDataset dataset) {
		chart.getXYPlot().setDataset(index, dataset);
	}
}
