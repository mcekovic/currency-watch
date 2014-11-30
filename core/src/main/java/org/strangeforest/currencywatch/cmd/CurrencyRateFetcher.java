package org.strangeforest.currencywatch.cmd;

import java.text.*;
import java.util.*;

import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.mapdb.*;
import org.strangeforest.currencywatch.nbs.*;
import org.strangeforest.currencywatch.ui.*;

import com.beust.jcommander.*;

public class CurrencyRateFetcher implements AutoCloseable {

	@Parameter(names = {"-db", "-dbPathName"}, description = "DB path used to store currency rates.")
	private String dbPathName = System.getProperty("user.home") + DB_PATH_NAME;

	@Parameter(names = {"-c", "-currency"}, description = "Currency symbol.")
	private String currency = UIUtil.DEFAULT_CURRENCY.toString();

	@Parameter(names = "-from", description = "Date to fetch currency rates from in format dd-mm-yyyy.", converter = DateConverter.class)
	private Date from = Util.toDate(Util.START_DATE);

	@Parameter(names = "-to", description = "Date to fetch currency rates to in format dd-mm-yyyy.", converter = DateConverter.class)
	private Date to = Util.toDate(Util.getLastDate());

	@Parameter(names = {"-t", "-threads"}, description = "Number of threads to use for fetching.")
	private int threadCount = THREAD_COUNT;

	@Parameter(names = {"-?", "-h", "-help"}, description = "Shows usage.", help = true)
	private boolean help;

	private static final String DB_PATH_NAME = "/.currency-watch/data/currency-rates-db";
	private static final int THREAD_COUNT = 20;

	public static void main(String[] args) {
		try (CurrencyRateFetcher fetcher = new CurrencyRateFetcher()) {
			if (fetcher.parseArguments(args)) {
				fetcher.init();
				fetcher.fetch();
			}
		}
	}

	private ChainedCurrencyRateProvider provider;
	private DateRange dates;
	private int totalDays;
	private int fetchedDays, localFetchedDays;
	private long startTime;

	private boolean parseArguments(String[] args) {
		try {
			JCommander cmd = new JCommander(this, args);
			if (help)
				cmd.usage();
			else
				return true;
		}
		catch (ParameterException ex) {
			System.out.println(ex.getMessage());
			System.out.println("Use -? to get help on usage.");
		}
		return false;
	}

	private void init() {
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider(rateEvent -> incFetchedAndPrintProgress());
		provider = new ChainedCurrencyRateProvider(
			new MapDBCurrencyRateProvider(dbPathName),
			new ParallelCurrencyRateProviderProxy(remoteProvider, threadCount)
		);
		provider.init();
		dates = new DateRange(from, to);
		totalDays = dates.size();
	}

	public void fetch() {
		System.out.printf("Fetching currency rates for currency %1$s from %2$td-%2$tm-%2$tY to %3$td-%3$tm-%3$tY...%n", currency, from, to);
		initAndPrintProgress();
		startTime = System.currentTimeMillis();
		try (CurrencyRates currencyRates = new CurrencyRates(Util.BASE_CURRENCY, currency, provider)) {
			currencyRates.getRates(dates);
			System.out.printf("%nFetching finished.%n");
		}
	}

	@Override public void close() {
		if (provider != null)
			provider.close();
	}

	private synchronized void initAndPrintProgress() {
		try (CurrencyRates currencyRates = new CurrencyRates(Util.BASE_CURRENCY, currency, provider.getLocalProvider())) {
			localFetchedDays = currencyRates.getRates(dates).size();
		}
		fetchedDays = localFetchedDays;
		System.out.printf("Completed: %1$5.1f%% (%2$d/%3$d)", (100.0*fetchedDays)/totalDays, fetchedDays, totalDays);
	}

	private synchronized void incFetchedAndPrintProgress() {
		fetchedDays++;
		double completed = (100.0*fetchedDays)/totalDays;
		long time = System.currentTimeMillis() - startTime;
		double datesPerSec = time > 0L ? (1000.0*(fetchedDays - localFetchedDays))/time : 0.0;
		System.out.printf("\rCompleted: %1$5.1f%% (%2$d/%3$d) at %4$6.1f rates/sec", completed, fetchedDays, totalDays, datesPerSec);
	}

	public static class DateConverter implements IStringConverter<Date> {
		@Override public Date convert(String s) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
				format.setLenient(false);
				return format.parse(s);
			}
			catch (ParseException ex) {
				throw new ParameterException(ex.getMessage());
			}
		}
	}
}
