package org.strangeforest.currencywatch.app;

import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.db4o.*;
import org.strangeforest.currencywatch.nbs.*;

import com.finsoft.util.*;

public class CurrencyWatch {

	//TODO Use command line parameters
	private static final String DB4O_DATA_FILE = "data/currency-rates.db4o";
	private static final int REMOTE_PROVIDER_THREAD_COUNT = 10;

	public static void main(String[] args) throws IOException {
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

	private static CurrencyRateProvider createProvider() {
		ObservableCurrencyRateProvider remoteProvider = new NBSCurrencyRateProvider();
		remoteProvider.addListener(new CurrencyRateAdapter() {
			@Override public void newRate(CurrencyRateEvent rateEvent) {
				System.out.println(rateEvent);
			}
		});
		CurrencyRateProvider provider = new ChainedCurrencyRateProvider(
			new Db4oCurrencyRateProvider(DB4O_DATA_FILE),
			new ParallelCurrencyRateProviderProxy(remoteProvider, REMOTE_PROVIDER_THREAD_COUNT)
		);
		provider.init();
		return provider;
	}
}
