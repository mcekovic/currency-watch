package org.strangeforest.currencywatch.ui;

import java.time.*;

import org.strangeforest.currencywatch.core.*;

import static org.strangeforest.currencywatch.Util.*;

public abstract class UIUtil {

	public static final Integer[] MOV_AVG_PERIODS = {10, 20, 50, 100, 200, 500};

	public static final CurrencySymbol DEFAULT_CURRENCY = CurrencySymbol.EUR;
	public static final Period DEFAULT_PERIOD = Period.MONTH;
	public static final SeriesQuality DEFAULT_QUALITY = SeriesQuality.NORMAL;
   public static final int DEFAULT_MOV_AVG_PERIOD = 20;

	public static int daysFromStart() {
		return dayDifference(toDate(START_DATE), toDate(getLastDate()));
	}

	public static DateRange toDateRange(int days) {
		LocalDate lastDate = getLastDate();
		LocalDate firstDate = lastDate.minusDays(days);
		return new DateRange(toDate(firstDate), toDate(lastDate));
	}
}
