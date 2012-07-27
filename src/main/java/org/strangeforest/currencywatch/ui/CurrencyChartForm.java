package org.strangeforest.currencywatch.ui;

import java.awt.event.*;
import java.io.*;
import java.text.*;
import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;

import com.finsoft.util.*;

public class CurrencyChartForm {

	private JButton exitButton;
	private JPanel formPanel;
	private ChartPanel chartPanel;
	private JComboBox<String> periodComboBox;
	private JComboBox<String> currencyComboBox;
	private JPanel toolPanel;
	private JProgressBar progressBar;
	private JComboBox<String> qualityComboBox;
	private JCheckBox movAvgCheckBox;
	private JComboBox<Integer> movAvgPeriodComboBox;
	private JPanel statusPanel;
	private JLabel speedLabel;

	private CurrencyRatePresenter presenter;

	public static void main(String[] args) throws IOException {
		final CurrencyChartForm form = new CurrencyChartForm();
		form.inputDataChanged();
		final JFrame frame = new JFrame(String.format("Currency Watch %s", ManifestUtil.getManifestAttribute("currency-watch", "Implementation-Version")));
		frame.setContentPane(form.formPanel);
		form.exitButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				form.presenter.dispose();
				frame.dispose();
			}
		});
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public CurrencyChartForm() {
		presenter = new CurrencyRatePresenter(chartPanel, progressBar, speedLabel);
		currencyComboBox.setModel(new DefaultComboBoxModel<>(CurrencyRatePresenter.CURRENCIES));
		currencyComboBox.setSelectedItem(CurrencyRatePresenter.DEFAULT_CURRENCY);
		currencyComboBox.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				inputDataChanged();
			}
		});
		periodComboBox.setModel(new DefaultComboBoxModel<>(CurrencyRatePresenter.periods()));
		periodComboBox.setSelectedItem(CurrencyRatePresenter.DEFAULT_PERIOD);
		periodComboBox.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				inputDataChanged();
			}
		});
		qualityComboBox.setModel(new DefaultComboBoxModel<>(CurrencyRatePresenter.qualities()));
		qualityComboBox.setSelectedItem(CurrencyRatePresenter.DEFAULT_QUALITY);
		qualityComboBox.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				inputDataChanged();
			}
		});
		movAvgCheckBox.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				movAvgPeriodComboBox.setEnabled(movAvgCheckBox.isSelected());
				inputDataChanged();
			}
		});
		movAvgPeriodComboBox.setModel(new DefaultComboBoxModel<>(CurrencyRatePresenter.MOV_AVG_PERIODS));
		movAvgPeriodComboBox.setSelectedItem(CurrencyRatePresenter.DEFAULT_MOV_AVG_PERIOD);
		movAvgPeriodComboBox.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				inputDataChanged();
			}
		});
	}

	private void createUIComponents() {
		JFreeChart chart = ChartFactory.createXYLineChart(null, "Date", "Middle", null, PlotOrientation.VERTICAL, false, true, false);
		chart.setAntiAlias(true);
		chart.getXYPlot().setDomainAxis(new DateAxis("Date"));
		NumberAxis rangeAxis = ((NumberAxis)chart.getXYPlot().getRangeAxis());
		rangeAxis.setAutoRangeIncludesZero(false);
		chart.getXYPlot().getRenderer().setBaseToolTipGenerator(new StandardXYToolTipGenerator(
			StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
			new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")
		));
		chartPanel = new ChartPanel(chart);
	}

	private void inputDataChanged() {
		presenter.inputDataChanged(
			(String)currencyComboBox.getSelectedItem(),
			(String)periodComboBox.getSelectedItem(),
			(String)qualityComboBox.getSelectedItem(),
			movAvgCheckBox.isSelected(),
			(Integer)movAvgPeriodComboBox.getSelectedItem()
		);
	}
}
