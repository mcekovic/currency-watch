package org.strangeforest.currencywatch.web;

import java.util.*;
import javax.faces.bean.*;
import javax.faces.model.*;

import org.strangeforest.currencywatch.ui.CurrencySymbol;
import org.strangeforest.currencywatch.ui.*;

import com.finsoft.util.*;

import static com.finsoft.util.Algorithms.*;
import static java.util.Arrays.*;

@ManagedBean(name = "chartBean")
@RequestScoped
public class ChartBean {

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

	public List<SelectItem> getSeriesQualities() {
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

	public SeriesQuality getDefaultSeriesQuality() {
		return UIUtil.DEFAULT_SERIES_QUALITY;
	}
}
