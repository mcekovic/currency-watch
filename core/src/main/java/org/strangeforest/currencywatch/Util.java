package org.strangeforest.currencywatch;

import java.util.*;

import org.joda.time.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.util.*;

public abstract class Util {

	public static final String BASE_CURRENCY = "RSD";

	public static final Calendar START_DATE = new GregorianCalendar(2002, 5, 15);

	public static final long MILLISECONDS_PER_DAY = new Period(1, PeriodType.days()).getMillis();

	public static Calendar getLastDate() {
		Calendar now = new GregorianCalendar();
		Calendar lastDate = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
		if (now.get(Calendar.HOUR_OF_DAY) < 8)
			lastDate.add(Calendar.DATE, -1);
		return lastDate;
	}

	public static DateRange trimDateRange(DateRange range) {
		Date minFrom = START_DATE.getTime();
		Date maxTo = getLastDate().getTime();
		return new DateRange(ObjectUtil.max(range.getFrom(), minFrom), ObjectUtil.min(range.getTo(), maxTo));
	}

	public static int dayDifference(Date fromDate, Date toDate) {
		return new Period(new DateTime(fromDate), new DateTime(toDate)).getDays();
	}

	public static Date extractDate(Date date) {
		return new DateTime(date).toLocalDate().toDateTimeAtStartOfDay().toDate();
	}
}
