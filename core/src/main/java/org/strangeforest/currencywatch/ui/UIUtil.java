package org.strangeforest.currencywatch.ui;

import java.util.*;

import com.finsoft.util.*;

public abstract class UIUtil {

	public static final String BASE_CURRENCY = "RSD";

	public static final Calendar START_DATE = new GregorianCalendar(2002, 5, 15);

	public static final Integer[] MOV_AVG_PERIODS = {10, 20, 50, 100, 200, 500};
	public static final CurrencySymbol DEFAULT_CURRENCY = CurrencySymbol.EUR;
	public static final Period DEFAULT_PERIOD = Period.MONTH;
	public static final SeriesQuality DEFAULT_QUALITY = SeriesQuality.NORMAL;

	public static final int DEFAULT_MOV_AVG_PERIOD = 20;

	public static Calendar getLastDate() {
		Calendar now = new GregorianCalendar();
		Calendar lastDate = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		if (now.get(Calendar.HOUR_OF_DAY) < 8)
			lastDate.add(Calendar.DATE, -1);
		return lastDate;
	}

	public static int daysFromStart() {
		return DateUtil.dayDifference(START_DATE.getTime(), getLastDate().getTime());
	}
}
