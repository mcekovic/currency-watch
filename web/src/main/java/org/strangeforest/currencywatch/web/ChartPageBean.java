package org.strangeforest.currencywatch.web;

import java.io.*;
import java.util.*;
import javax.faces.bean.*;
import javax.faces.context.*;
import javax.faces.model.*;
import javax.servlet.http.*;

import org.jfree.chart.servlet.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;

import com.finsoft.util.*;

import static com.finsoft.util.Algorithms.*;
import static java.util.Arrays.*;

@ManagedBean(name = "chartPage")
@RequestScoped
public class ChartPageBean {

	@ManagedProperty("#{chartApp}")
	private ChartAppBean chartApp;

	private CurrencySymbol currency = UIUtil.DEFAULT_CURRENCY;
	private Period period = UIUtil.DEFAULT_PERIOD;
	private SeriesQuality quality = UIUtil.DEFAULT_QUALITY;
	private String chartFileName;

	public ChartAppBean getChartApp() {
		return chartApp;
	}

	public void setChartApp(ChartAppBean chartApp) {
		this.chartApp = chartApp;
	}

	public CurrencySymbol getCurrency() {
		return currency;
	}

	public void setCurrency(CurrencySymbol currency) {
		this.currency = currency;
	}

	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public SeriesQuality getQuality() {
		return quality;
	}

	public void setQuality(SeriesQuality quality) {
		this.quality = quality;
	}

	public String getChartFileName() {
		return chartFileName;
	}

	public List<SelectItem> getCurrencies() {
		return transform(asList(CurrencySymbol.values()), new Transformer<CurrencySymbol, SelectItem>() {
			@Override public SelectItem transform(CurrencySymbol currency) {
				String symbol = currency.toString();
				return new SelectItem(symbol, symbol, currency.description());
			}
		});
	}

	public List<SelectItem> getPeriods() {
		return transform(asList(Period.values()), new Transformer<Period, SelectItem>() {
			@Override public SelectItem transform(Period period) {
				return new SelectItem(period, period.label());
			}
		});
	}

	public List<SelectItem> getQualities() {
		return transform(asList(SeriesQuality.values()), new Transformer<SeriesQuality, SelectItem>() {
			@Override public SelectItem transform(SeriesQuality quality) {
				return new SelectItem(quality, quality.label());
			}
		});
	}

	public String showChart() throws IOException {
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		CurrencyChart chart = new CurrencyChart();
		chart.createSeries(currency, false, false, false);

		int days = period.days();
		DateRange dateRange = UIUtil.toDateRange(days);
		chart.setDateRange(dateRange);

		CurrencyRate currencyRate = new CurrencyRate(UIUtil.BASE_CURRENCY, currency.name(), chartApp.getProvider());
		Map<Date, RateValue> rates = currencyRate.getRates(dateRange.dates(1 + days/quality.points()));

		chart.updateBaseSeries(rates);
		chart.updateDerivedSeries(20);
		chartFileName = ServletUtilities.saveChartAsPNG(chart.getChart(), 1000, 600, session);

		return "currency-chart.xhtml";
	}
}
