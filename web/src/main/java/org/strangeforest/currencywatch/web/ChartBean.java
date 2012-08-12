package org.strangeforest.currencywatch.web;

import java.io.*;
import java.util.*;
import javax.faces.bean.*;
import javax.faces.context.*;
import javax.faces.model.*;
import javax.servlet.http.*;

import org.jfree.chart.servlet.*;
import org.strangeforest.currencywatch.ui.CurrencySymbol;
import org.strangeforest.currencywatch.ui.*;

import com.finsoft.util.*;

import static com.finsoft.util.Algorithms.*;
import static java.util.Arrays.*;

@ManagedBean(name = "chartBean")
@RequestScoped
public class ChartBean {

	private CurrencySymbol currency;
	private Period period;
	private SeriesQuality quality;
	private String chartFileName;

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

	public CurrencySymbol getDefaultCurrency() {
		return UIUtil.DEFAULT_CURRENCY;
	}

	public Period getDefaultPeriod() {
		return UIUtil.DEFAULT_PERIOD;
	}

	public SeriesQuality getDefaultQuality() {
		return UIUtil.DEFAULT_QUALITY;
	}

	public String showChart() throws IOException {
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		chartFileName = ServletUtilities.saveChartAsPNG(null, 1000, 600, session);
		return "currency-chart.xhtml";
	}
}
