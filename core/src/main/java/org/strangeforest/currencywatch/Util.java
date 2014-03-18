package org.strangeforest.currencywatch;

import java.time.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.util.*;

public abstract class Util {

	public static final String BASE_CURRENCY = "RSD";

	public static final LocalDate START_DATE = LocalDate.of(2002, 5, 15);

	public static final long MILLISECONDS_PER_DAY = Duration.ofDays(1).toMillis();

	public static LocalDate getLastDate() {
		LocalDate lastDate = LocalDate.now();
		if (LocalTime.now().isBefore(LocalTime.of(8, 0)))
			lastDate = lastDate.minusDays(1);
		return lastDate;
	}

	public static Date toDate(LocalDate date) {
		return new Date(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}

	public static LocalDate toLocalDate(Date date) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault()).toLocalDate();
	}

	public static DateRange trimDateRange(DateRange range) {
		Date minFrom = toDate(START_DATE);
		Date maxTo = toDate(getLastDate());
		return new DateRange(ObjectUtil.max(range.getFrom(), minFrom), ObjectUtil.min(range.getTo(), maxTo));
	}

	public static int dayDifference(Date fromDate, Date toDate) {
		return (int)Duration.between(Instant.ofEpochMilli(fromDate.getTime()), Instant.ofEpochMilli(toDate.getTime())).toDays();
	}

	public static Date extractDate(Date date) {
		return toDate(LocalDate.from(Instant.ofEpochMilli(date.getTime())));
	}
}
