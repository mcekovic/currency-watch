package org.strangeforest.currencywatch.ui;

import java.util.*;

import com.finsoft.util.*;

public abstract class UIUtil {

	public static final String SYMBOL_FROM = "DIN";

	public static final Calendar START_DATE = new GregorianCalendar(2002, 5, 15);

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
