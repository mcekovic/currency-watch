package test.strangeforest.currencywatch;

import java.time.*;
import java.util.*;

import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;

import static org.strangeforest.currencywatch.Util.*;

public class TestData {

	public static final String BASE_CURRENCY = Util.BASE_CURRENCY;
	public static final String CURRENCY = "EUR";

	public static final Date DATE = toDate(LocalDate.of(2012, 4, 1));
	public static final RateValue RATE = new RateValue("116.0", "120.0", "118.0");

	public static final Date DATE1 = toDate(LocalDate.of(2012, 4, 1));
	public static final Date DATE2 = toDate(LocalDate.of(2012, 4, 2));
	public static final Date DATE3 = toDate(LocalDate.of(2012, 4, 3));
	public static final Date DATE4 = toDate(LocalDate.of(2012, 4, 4));
	public static final Date DATE5 = toDate(LocalDate.of(2012, 4, 5));

	public static final NavigableMap<Date, RateValue> RATES = new TreeMap<>();
	static {
		TestData.RATES.put(TestData.DATE1, new RateValue("116.0", "120.0", "118.0"));
		TestData.RATES.put(TestData.DATE2, new RateValue("117.0", "121.0", "119.0"));
		TestData.RATES.put(TestData.DATE3, new RateValue("116.5", "120.5", "118.5"));
		TestData.RATES.put(TestData.DATE4, new RateValue("117.1", "121.1", "119.1"));
		TestData.RATES.put(TestData.DATE5, new RateValue("116.7", "120.7", "118.7"));
	}

	public static final NavigableSet<Date> DATES = RATES.navigableKeySet();
	public static final List<Date> DATE_LIST = new ArrayList<>(DATES);

	public static final Date UNKNOWN_DATE = toDate(LocalDate.of(2000, 1, 1));
}
