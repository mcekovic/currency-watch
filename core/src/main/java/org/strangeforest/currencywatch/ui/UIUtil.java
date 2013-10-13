package org.strangeforest.currencywatch.ui;

import java.util.*;

import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;

public abstract class UIUtil {

	public static final Integer[] MOV_AVG_PERIODS = {10, 20, 50, 100, 200, 500};

	public static final CurrencySymbol DEFAULT_CURRENCY = CurrencySymbol.EUR;
	public static final Period DEFAULT_PERIOD = Period.MONTH;
	public static final SeriesQuality DEFAULT_QUALITY = SeriesQuality.NORMAL;
   public static final int DEFAULT_MOV_AVG_PERIOD = 20;

	public static int daysFromStart() {
		return Util.dayDifference(Util.START_DATE.getTime(), Util.getLastDate().getTime());
	}

	public static DateRange toDateRange(int days) {
		Calendar cal = Util.getLastDate();
		Date toDate = cal.getTime();
		cal.add(Calendar.DATE, -days);
		Date fromDate = cal.getTime();
		return new DateRange(fromDate, toDate);
	}
}
