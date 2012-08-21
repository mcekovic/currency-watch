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
	private String dbFileName = System.getProperty("user.home") + DB_FILE_NAME;

	@Parameter(names = {"-r", "-useRest"}, description = "Fetch data from REST API.")
	private boolean useRest = false;

	@Parameter(names = {"-u", "-url"}, description = "REST API URL to fetch data from.")
	private String url;

	@Parameter(names = {"-b", "-batch"}, description = "Batch size to use for fetching from REST service.")
	private int batchSize = REMOTE_PROVIDER_BATCH_SIZE;

	@Parameter(names = {"-t", "-threads"}, description = "Number of threads to use for fetching from NBS.")
	private int threadCount = REMOTE_PROVIDER_THREAD_COUNT;

	@Parameter(names = {"-?", "-h", "-help"}, description = "Shows usage.", help = true)
	private boolean help;

	private static final String DB_FILE_NAME = "/.currency-watch/data/currency-rates.db4o";
	private static final int REMOTE_PROVIDER_BATCH_SIZE   = 50;
	private static final int REMOTE_PROVIDER_THREAD_COUNT = 10;

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyWatch.class);

	public static void main(String[] args) throws Exception {
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
		if (url != null) {
			CurrencyRateProvider provider = tryUseRESTProvider(url.equals(LOCAL_URL) ? LOCAL_REST_URI : URI.create(url));
			if (provider != null)
				return provider;
		}
		if (useRest) {
			for (URI uri : REST_URIS) {
				CurrencyRateProvider provider = tryUseRESTProvider(uri);
				if (provider != null)
					return provider;
			}
		}
		LOGGER.info("Using NBSCurrencyRateProvider.");
		return createNBSProvider();
	}

	private static final String LOCAL_URL = "local";
	private static final URI LOCAL_REST_URI = URI.create("http://localhost:8080/currency-watch-web-1.3/api");

	private static final URI[] REST_URIS = new URI[] {
		URI.create("http://ubuntu.beg.finsoft.com:8080/currency-watch/api")
	};

	private CurrencyRateProvider tryUseRESTProvider(URI uri) {
		RESTCurrencyWatchProvider provider = createRESTProvider(uri);
		if (provider != null) {
			LOGGER.info("Using RESTCurrencyWatchProvider at " + uri);
			return new BatchingCurrencyRateProviderProxy(provider, batchSize);
		}
		else {
			LOGGER.warn("Unable to use RESTCurrencyWatchProvider at " + uri);
			return null;
		}
	}

	private RESTCurrencyWatchProvider createRESTProvider(URI uri) {
		try (RESTCurrencyWatchProvider provider = new RESTCurrencyWatchProvider(uri)) {
			provider.init();
			return provider.ping() ? provider : null;
		}
		catch (Exception ex) {
			LOGGER.debug("Error pinging RESTCurrencyWatchProvider.", ex);
			return null;
		}
	}

	private CurrencyRateProvider createNBSProvider() {
		ObservableCurrencyRateProvider nbsProvider = new NBSCurrencyRateProvider(new CurrencyRateTracer());
		return new ParallelCurrencyRateProviderProxy(nbsProvider, threadCount);
	}
}