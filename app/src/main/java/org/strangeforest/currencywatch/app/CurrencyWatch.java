package org.strangeforest.currencywatch.app;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

import org.slf4j.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;
import org.strangeforest.currencywatch.rest.*;

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

	public static void main(String[] args) throws IOException, URISyntaxException {
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

	private void launchApp() throws IOException, URISyntaxException {
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

	private CurrencyRateProvider createProvider() throws URISyntaxException {
		CurrencyRateProvider provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider(dbFileName),
			createRemoteProvider()
		);
		provider.init();
		return provider;
	}

	private CurrencyRateProvider createRemoteProvider() throws URISyntaxException {
		RESTCurrencyWatchProvider restProvider = createRESTProvider();
		restProvider.init();
		if (restProvider.ping()) {
			restProvider.close();
			LOGGER.info("Using RESTCurrencyWatchProvider.");
			return restProvider;
		}
		else {
			LOGGER.info("Using NBSCurrencyRateProvider.");
			return createNBSProvider();
		}
	}

	private CurrencyRateProvider createNBSProvider() {
		ObservableCurrencyRateProvider nbsProvider = new NBSCurrencyRateProvider();
		nbsProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug(rateEvent.toString());
			}
		});
		return new ParallelCurrencyRateProviderProxy(nbsProvider, threadCount);
	}

	private RESTCurrencyWatchProvider createRESTProvider() throws URISyntaxException {
//		return new RESTCurrencyWatchProvider(new URI("http://ubuntu.beg.finsoft.com:8080/currency-watch/api"));
		return new RESTCurrencyWatchProvider(new URI("http://localhost:8080/currency-watch-web-1.3/api"));
	}
}