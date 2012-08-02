package org.strangeforest.currencywatch.cmd;

import java.text.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;
import org.strangeforest.currencywatch.ui.*;

import com.beust.jcommander.*;

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
	private Date from = UIUtil.START_DATE.getTime();

	@Parameter(names = "-to", description = "Date to fetch currency rates to in format dd-mm-yyyy.", converter = DateConverter.class)
	private Date to = UIUtil.getLastDate().getTime();

	@Parameter(names = {"-t", "-threads"}, description = "Number of threads to use for fetching.")
	private int threadCount = THREAD_COUNT;

	@Parameter(names = {"-?", "-h", "-help"}, description = "Shows usage.", help = true)
	private boolean help;

	private ChainedCurrencyRateProvider provider;
	private DateRange dates;
	private int totalDays;
	private int fetchedDays;

	private void init() {
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider();
		remoteProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				incFetchedAndPrintProgress();
			}
		});
		Db4oCurrencyRateProvider localProvider = new Db4oCurrencyRateProvider(dbFileName);
		provider = new ChainedCurrencyRateProvider(
			localProvider,
			new ParallelCurrencyRateProviderProxy(remoteProvider, threadCount)
		);
		provider.init();
		dates = new DateRange(from, to);
		totalDays = dates.size();
	}

	public void fetch() {
		System.out.printf("Fetching currency rates for symbol %1$s from %2$td-%2$tm-%2$tY to %3$td-%3$tm-%3$tY...\n", symbolTo, from, to);
		initAndPrintProgress();
		try (CurrencyRate currencyRate = new CurrencyRate(UIUtil.SYMBOL_FROM, symbolTo, provider)) {
			currencyRate.getRates(dates);
			System.out.println("\nFetching finished.");
		}
	}

	@Override public void close() {
		if (provider != null)
			provider.close();
	}

	private synchronized void initAndPrintProgress() {
		try (CurrencyRate currencyRate = new CurrencyRate(UIUtil.SYMBOL_FROM, symbolTo, provider.getLocalProvider())) {
			fetchedDays = currencyRate.getRates(dates).size();
		}
		printProgress();
	}

	private synchronized void incFetchedAndPrintProgress() {
		fetchedDays++;
		printProgress();
	}

	private void printProgress() {
		System.out.printf("\rCompleted: %1$5.1f%% (%2$d/%3$d)", (100.0 * fetchedDays) / totalDays, fetchedDays, totalDays);
	}

	public static class DateConverter implements IStringConverter<Date> {
		@Override public Date convert(String s) {
			try {
				return new SimpleDateFormat("dd-MM-yyyy").parse(s);
			}
			catch (ParseException ex) {
				throw new IllegalArgumentException(s);
			}
		}
	}
}
