package org.strangeforest.currencywatch.web.jsf;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.faces.bean.*;
import javax.faces.context.*;
import javax.faces.model.*;
import javax.servlet.http.*;

import org.jfree.chart.*;
import org.jfree.chart.servlet.*;
import org.slf4j.*;
import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;

import com.finsoft.util.*;

import static com.finsoft.util.Algorithms.*;
import static java.util.Arrays.*;

@ManagedBean(name = "chartPage")
@RequestScoped
public class ChartPageBean {

	@ManagedProperty("#{currencyRateProvider}")
	private CurrencyRateProvider currencyProvider;

	private CurrencySymbol currency = UIUtil.DEFAULT_CURRENCY;
	private Period period = UIUtil.DEFAULT_PERIOD;
	private SeriesQuality quality = UIUtil.DEFAULT_QUALITY;
	private boolean showBidAsk;
	private boolean showMovAvg;
	private boolean showBollBands;
	private Integer movAvgPeriod = UIUtil.DEFAULT_MOV_AVG_PERIOD;
	private DateRange dateRange;
	private double zoomx1, zoomx2;

	private CurrentRate currentRate;
	private String chartFileName;
	private boolean zoomed;

	private static final String PAGE_TITLE = "Currency Chart";
	private static final int CHART_WIDTH  = 1000;
	private static final int CHART_HEIGHT =  600;
	private static final int OFFSET_X1 = 65;
	private static final int OFFSET_X2 =  8;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("ddMMyyyy");

	private static final Logger LOGGER = LoggerFactory.getLogger(ChartPageBean.class);

	public CurrencyRateProvider getCurrencyProvider() {
		return currencyProvider;
	}

	public void setCurrencyProvider(CurrencyRateProvider currencyProvider) {
		this.currencyProvider = currencyProvider;
	}

	public String getTitle() {
		return chartFileName == null ? PAGE_TITLE : String.format("%1$s - %2$s", PAGE_TITLE, getCurrencyFullName());
	}

	public CurrencySymbol getCurrency() {
		return currency;
	}

	public void setCurrency(CurrencySymbol currency) {
		this.currency = currency;
	}

	public String getCurrencyFullName() {
		return String.format("%1$s (%2$s)", currency, currency.description());
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

	public boolean isShowBidAsk() {
		return showBidAsk;
	}

	public void setShowBidAsk(boolean showBidAsk) {
		this.showBidAsk = showBidAsk;
	}

	public boolean isShowMovAvg() {
		return showMovAvg;
	}

	public void setShowMovAvg(boolean showMovAvg) {
		this.showMovAvg = showMovAvg;
	}

	public boolean isShowBollBands() {
		return showBollBands;
	}

	public void setShowBollBands(boolean showBollBands) {
		this.showBollBands = showBollBands;
	}

	public Integer getMovAvgPeriod() {
		return movAvgPeriod;
	}

	public void setMovAvgPeriod(Integer movAvgPeriod) {
		this.movAvgPeriod = movAvgPeriod != null ? movAvgPeriod : UIUtil.DEFAULT_MOV_AVG_PERIOD;
	}

	public String getDateRange() {
		return dateRange != null ? DATE_FORMAT.format(dateRange.getFrom()) + '-' + DATE_FORMAT.format(dateRange.getTo()) : null;
	}

	public void setDateRange(String dateRange) throws ParseException {
		this.dateRange = dateRange.length() >= 17 ? new DateRange(DATE_FORMAT.parse(dateRange.substring(0, 8)), DATE_FORMAT.parse(dateRange.substring(9, 17))) : null;
	}

	public Date getDateFrom() {
		return dateRange != null ? dateRange.getFrom() : null;
	}

	public Date getDateTo() {
		return dateRange != null ? dateRange.getTo() : null;
	}

	public double getZoomx1() {
		return zoomx1;
	}

	public void setZoomx1(double zoomx1) {
		this.zoomx1 = zoomx1;
	}

	public double getZoomx2() {
		return zoomx2;
	}

	public void setZoomx2(double zoomx2) {
		this.zoomx2 = zoomx2;
	}

	public CurrentRate getCurrentRate() {
		return currentRate;
	}

	public String getChartFileName() {
		return chartFileName;
	}

	public int getChartWidth() {
		return CHART_WIDTH;
	}

	public int getChartHeight() {
		return CHART_HEIGHT;
	}

	public boolean isZoomed() {
		return zoomed;
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

	public List<SelectItem> getMovAvgPeriods() {
		return transform(asList(UIUtil.MOV_AVG_PERIODS), new Transformer<Integer, SelectItem>() {
			@Override public SelectItem transform(Integer movAvgPeriod) {
				return new SelectItem(movAvgPeriod, String.valueOf(movAvgPeriod));
			}
		});
	}

	public String showChart() throws IOException {
		DateRange dateRange = UIUtil.toDateRange(period.days());
		return doShowChart(dateRange, false);
	}

	public String zoomChart() throws IOException {
		double x1Pct = Math.max(0.0, (zoomx1-OFFSET_X1)/(CHART_WIDTH-OFFSET_X1-OFFSET_X2));
		double x2Pct = Math.min(1.0, (zoomx2-OFFSET_X1)/(CHART_WIDTH-OFFSET_X1-OFFSET_X2));
		int size = dateRange.size()-1;
		Date fromDate = new Date(dateRange.getFrom().getTime() + Math.round(size*x1Pct)*DateUtil.MILLISECONDS_PER_DAY);
		Date toDate = new Date(dateRange.getFrom().getTime() + Math.round(size*x2Pct)*DateUtil.MILLISECONDS_PER_DAY);
		if (fromDate.before(toDate)) {
			dateRange = new DateRange(fromDate, toDate);
			zoomed = true;
		}
		return doShowChart(dateRange, true);
	}

	private String doShowChart(DateRange dateRange, boolean autoRangeRangeAxis) throws IOException {
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		CurrencyChart chart = new CurrencyChart();
		chart.createSeries(currency, showBidAsk, showMovAvg, showBollBands);

		chart.setDateRange(dateRange);
		if (autoRangeRangeAxis)
			chart.setAutoRangeRangeAxis();
		this.dateRange = dateRange;

		CurrencyRate currencyRate = new CurrencyRate(Util.BASE_CURRENCY, currency.name(), currencyProvider);
		Map<Date, RateValue> rates;
		try {
			int step = 1 + dateRange.size()/quality.points();
			currencyRate.getRates(dateRange.dates(step*10)); // Fetch outline first
			rates = currencyRate.getRates(dateRange.dates(step));
		}
		catch (CurrencyRateException ex) {
			LOGGER.error("Error getting currency data.", ex);
			rates = currencyRate.getRates();
		}

		currentRate = CurrentRate.forRates(rates);

		chart.updateBaseSeries(rates);
		chart.updateDerivedSeries(movAvgPeriod);
		JFreeChart jFreeChart = chart.getChart();
		jFreeChart.setBackgroundPaint(Color.WHITE);
		chartFileName = ServletUtilities.saveChartAsPNG(jFreeChart, CHART_WIDTH, CHART_HEIGHT, session);

		return "app/currency-chart.xhtml";
	}
}
