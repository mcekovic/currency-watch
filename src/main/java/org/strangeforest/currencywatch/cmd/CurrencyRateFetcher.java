package org.strangeforest.currencywatch.cmd;

import java.text.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;

import com.beust.jcommander.*;
import com.db4o.foundation.*;

public class CurrencyRateFetcher implements AutoCloseable {

	private static final String DB_FILE_NAME = "data/currency-rates.db4o";
	private static final String SYMBOL_TO = "EUR";
	private static final int THREAD_COUNT = 20;

	public static void main(String[] args) {
		try (CurrencyRateFetcher fetcher = new CurrencyRateFetcher()) {
			JCommander cmd = new JCommander(fetcher, args);
			if (!fetcher.help) {
				fetcher.init();
				fetcher.fetch();
			}
			else
				cmd.usage();
		}
		catch (ParameterException ex) {
			System.out.println(ex.getMessage());
			System.out.println("Use -? to get help on usage.");
		}
	}

	@Parameter(names = {"-db", "-dbFileName"}, description = "DB file used to store currency rates.")
	private String dbFileName = DB_FILE_NAME;

	@Parameter(names = {"-s", "-symbol"}, description = "Currency symbol.")
	private String symbolTo = SYMBOL_TO;

	@Parameter(names = "-from", description = "Date to fetch currency rates from in format dd-mm-yyyy.", converter = DateConverter.class)
	private Date from = Util.START_DATE.getTime();

	@Parameter(names = "-to", description = "Date to fetch currency rates to in format dd-mm-yyyy.", converter = DateConverter.class)
	private Date to = Util.getLastDate().getTime();

	@Parameter(names = {"-t", "-threads"}, description = "Number of threads to use for fetching.")
	private int threadCount = THREAD_COUNT;

	@Parameter(names = {"-?", "-h", "-help"}, description = "Shows usage.", help = true)
	private boolean help;

	private ChainedCurrencyRateProvider provider;
	private DateRange dates;
	private int totalDays;
	private int fetchedDays, localFetchedDays;
	private long startTime;

	private void init() {
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider();
		remoteProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				incFetchedAndPrintProgress();
			}
		});
		provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider(dbFileName),
			new ParallelCurrencyRateProviderProxy(remoteProvider, threadCount)
		);
		provider.init();
		dates = new DateRange(from, to);
		totalDays = dates.size();
	}

	public void fetch() {
		System.out.printf("Fetching currency rates for symbol %1$s from %2$td-%2$tm-%2$tY to %3$td-%3$tm-%3$tY...\n", symbolTo, from, to);
		initAndPrintProgress();
		startTime = System.currentTimeMillis();
		try (CurrencyRate currencyRate = new CurrencyRate(Util.SYMBOL_FROM, symbolTo, provider)) {
			currencyRate.getRates(dates);
			System.out.println("\nFetching finished.");
		}
	}

	@Override public void close() {
		if (provider != null)
			provider.close();
	}

	private synchronized void initAndPrintProgress() {
		try (CurrencyRate currencyRate = new CurrencyRate(Util.SYMBOL_FROM, symbolTo, provider.getLocalProvider())) {
			localFetchedDays = currencyRate.getRates(dates).size();
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
