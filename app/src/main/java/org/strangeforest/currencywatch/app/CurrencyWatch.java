package org.strangeforest.currencywatch.app;

import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import org.slf4j.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;

import com.beust.jcommander.*;

import com.finsoft.util.*;

public class CurrencyWatch {

	@Parameter(names = {"-db", "-dbFileName"}, description = "DB file used to store currency rates.")
	private String dbFileName = DB_FILE_NAME;

	@Parameter(names = {"-t", "-threads"}, description = "Number of threads to use for fetching.")
	private int threadCount = REMOTE_PROVIDER_THREAD_COUNT;

	@Parameter(names = {"-?", "-h", "-help"}, description = "Shows usage.", help = true)
	private boolean help;

	private static final String DB_FILE_NAME = "data/currency-rates.db4o";
	private static final int REMOTE_PROVIDER_THREAD_COUNT = 10;

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyWatch.class);

	public static void main(String[] args) throws IOException {
		CurrencyWatch app = new CurrencyWatch();
		if (app.parseArguments(args))
			app.launchApp();
	}

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

	private void launchApp() throws IOException {
		final CurrencyRateProvider provider = createProvider();
		final CurrencyRatePresenter presenter = new CurrencyRatePresenter(provider);
		JFrame frame = new JFrame(String.format("Currency Watch %s", ManifestUtil.getManifestAttribute("currency-watch", "Implementation-Version")));
		CurrencyChartForm form = new CurrencyChartForm(presenter, frame);
		frame.setContentPane(form.formPanel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosed(WindowEvent e) {
				presenter.close();
				provider.close();
			}
		});
		form.inputDataChanged();
	}

	private CurrencyRateProvider createProvider() {
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider();
		remoteProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug(rateEvent.toString());
			}
		});
		CurrencyRateProvider provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider(dbFileName),
			new ParallelCurrencyRateProviderProxy(remoteProvider, threadCount)
		);
		provider.init();
		return provider;
	}
}
