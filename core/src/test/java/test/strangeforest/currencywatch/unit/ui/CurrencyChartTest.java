package test.strangeforest.currencywatch.unit.ui;

import org.jfree.chart.*;
import org.jfree.data.xy.*;
import org.junit.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;

import static org.junit.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyChartTest {

	@Test
	public void simpleChartIsCreated() {
		CurrencyChart currencyChart = new CurrencyChart();
		currencyChart.createSeries(CurrencySymbol.EUR, false, false, false);
		currencyChart.setDateRange(new DateRange(DATE1, DATE5));
		currencyChart.updateBaseSeries(RATES);

		JFreeChart chart = currencyChart.getChart();
		assertEquals(2, chart.getXYPlot().getDatasetCount());

		XYDataset dataSet = chart.getXYPlot().getDataset();
		assertEquals(1, dataSet.getSeriesCount());
		assertEquals(RATES.size(), dataSet.getItemCount(0));

		XYDataset bbDataSet = chart.getXYPlot().getDataset(1);
		assertNull(bbDataSet);
	}

	@Test
	public void fullChartIsCreated() {
		CurrencyChart currencyChart = new CurrencyChart();
		currencyChart.createSeries(CurrencySymbol.USD, true, true, true);
		currencyChart.setDateRange(new DateRange(DATE1, DATE5));
		currencyChart.updateBaseSeries(RATES);
		currencyChart.updateDerivedSeries(2);

		JFreeChart chart = currencyChart.getChart();
		assertEquals(2, chart.getXYPlot().getDatasetCount());

		XYDataset dataSet = chart.getXYPlot().getDataset(0);
		assertEquals(4, dataSet.getSeriesCount());
		assertEquals(RATES.size(), dataSet.getItemCount(0));
		assertEquals(RATES.size(), dataSet.getItemCount(1));
		assertEquals(RATES.size(), dataSet.getItemCount(2));
		assertEquals(RATES.size(), dataSet.getItemCount(3));

		XYDataset bbDataSet = chart.getXYPlot().getDataset(1);
		assertEquals(2, bbDataSet.getSeriesCount());
		assertEquals(RATES.size(), bbDataSet.getItemCount(0));
		assertEquals(RATES.size(), bbDataSet.getItemCount(1));
	}
}
