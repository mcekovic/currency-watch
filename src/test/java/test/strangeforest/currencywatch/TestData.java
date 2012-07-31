package test.strangeforest.currencywatch;

import java.util.*;

import org.strangeforest.currencywatch.core.*;

public class TestData {

	public static final String SYMBOL_FROM = "DIN";
	public static final String SYMBOL_TO = "EUR";

	public static final Date DATE = new GregorianCalendar(2012, 4, 1).getTime();
	public static final RateValue RATE = new RateValue(120.0, 116.0, 118.0);

	public static final Date DATE1 = new GregorianCalendar(2012, 4, 1).getTime();
	public static final Date DATE2 = new GregorianCalendar(2012, 4, 2).getTime();
	public static final Date DATE3 = new GregorianCalendar(2012, 4, 3).getTime();
	public static final Date DATE4 = new GregorianCalendar(2012, 4, 4).getTime();
	public static final Date DATE5 = new GregorianCalendar(2012, 4, 5).getTime();

	public static final NavigableMap<Date, RateValue> RATES = new TreeMap<>();
	static {
		TestData.RATES.put(TestData.DATE1, new RateValue(120.0, 116.0, 118.0));
		TestData.RATES.put(TestData.DATE2, new RateValue(121.0, 117.0, 119.0));
		TestData.RATES.put(TestData.DATE3, new RateValue(120.5, 116.5, 118.5));
		TestData.RATES.put(TestData.DATE4, new RateValue(121.1, 117.1, 119.1));
		TestData.RATES.put(TestData.DATE5, new RateValue(120.7, 116.7, 118.7));
	}

	public static final NavigableSet<Date> DATES = RATES.navigableKeySet();

	public static final String DB4O_DATA_FILE = "data/test-rates-db4o.db4o";
}
