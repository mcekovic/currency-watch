package test.strangeforest.currencywatch.unit.ui;

import org.jfree.chart.*;
import org.jfree.data.xy.*;
import org.junit.*;
import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;

import static org.assertj.core.api.Assertions.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyChartTest {

	@Test
	public void simpleChartIsCreated() {
		CurrencyChart currencyChart = new CurrencyChart();
		currencyChart.createSeries(CurrencySymbol.EUR, false, false, false);
		currencyChart.setDateRange(new DateRange(DATE1, DATE5));
		currencyChart.updateBaseSeries(RATES);

		JFreeChart chart = currencyChart.getChart();
		assertThat(chart.getXYPlot().getDatasetCount()).isEqualTo(2);

		XYDataset dataSet = chart.getXYPlot().getDataset();
		assertThat(dataSet.getSeriesCount()).isEqualTo(1);
		assertThat(dataSet.getItemCount(0)).isEqualTo(RATES.size());

		XYDataset bbDataSet = chart.getXYPlot().getDataset(1);
		assertThat(bbDataSet).isNull();
	}

	@Test
	public void fullChartIsCreated() {
		CurrencyChart currencyChart = new CurrencyChart();
		currencyChart.createSeries(CurrencySymbol.USD, true, true, true);
		currencyChart.setDateRange(new DateRange(DATE1, DATE5));
		currencyChart.updateBaseSeries(RATES);
		currencyChart.updateDerivedSeries(2);
		CurrencyEvent event = new CurrencyEvent(Util.BASE_CURRENCY, DATE2, "A title", "A description");
		currencyChart.addAnnotation(event);

		JFreeChart chart = currencyChart.getChart();
		assertThat(chart.getXYPlot().getDatasetCount()).isEqualTo(2);

		XYDataset dataSet = chart.getXYPlot().getDataset(0);
		assertThat(dataSet.getSeriesCount()).isEqualTo(4);
		assertThat(dataSet.getItemCount(0)).isEqualTo(RATES.size());
		assertThat(dataSet.getItemCount(1)).isEqualTo(RATES.size());
		assertThat(dataSet.getItemCount(2)).isEqualTo(RATES.size());
		assertThat(dataSet.getItemCount(3)).isEqualTo(RATES.size());

		XYDataset bbDataSet = chart.getXYPlot().getDataset(1);
		assertThat(bbDataSet.getSeriesCount()).isEqualTo(2);
		assertThat(bbDataSet.getItemCount(0)).isEqualTo(RATES.size());
		assertThat(bbDataSet.getItemCount(1)).isEqualTo(RATES.size());

		assertThat(chart.getXYPlot().getAnnotations()).hasSize(1);
	}
}
