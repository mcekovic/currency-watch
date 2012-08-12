package org.strangeforest.currencywatch.app;

import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import com.finsoft.util.*;

public class CurrencyWatch {

	public static void main(String[] args) throws IOException {
		final CurrencyRatePresenter presenter = new CurrencyRatePresenter();
		final JFrame frame = new JFrame(String.format("Currency Watch %s", ManifestUtil.getManifestAttribute("currency-watch", "Implementation-Version")));
		CurrencyChartForm form = new CurrencyChartForm(presenter, frame);
		frame.setContentPane(form.formPanel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosed(WindowEvent e) {
				presenter.close();
			}
		});
		form.inputDataChanged();
	}
}
