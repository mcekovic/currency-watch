package org.strangeforest.currencywatch.app;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.strangeforest.currencywatch.ui.*;

import com.finsoft.util.*;

import static org.strangeforest.currencywatch.ui.UIUtil.*;

public class CurrencyChartForm {

	private JButton exitButton;
	private JPanel formPanel;
	private JPanel toolPanel;
	private JComboBox<CurrencySymbol> currencyComboBox;
	private JComboBox<Period> periodComboBox;
	private JComboBox<SeriesQuality> qualityComboBox;
	private JCheckBox bidAskCheckBox;
	private JCheckBox movAvgCheckBox;
	private JCheckBox bollingerBandsCheckBox;
	private JComboBox<Integer> movAvgPeriodComboBox;
	private ChartPanel chartPanel;
	private JPanel statusPanel;
	private JLabel speedLabel;
	private JProgressBar progressBar;
	private JLabel statusLabel;

	private CurrencyRatePresenter presenter;

	private static final Color MIDDLE_COLOR = new Color(255, 0, 0);
	private static final Color BID_COLOR = new Color(255, 128, 128);
	private static final Color ASK_COLOR = new Color(255, 128, 128);
	private static final Color MOV_AVG_COLOR = new Color(0, 0, 255);
	private static final Color BOLL_BANDS_COLOR = new Color(192, 224, 255, 64);
	private static final Color BOLL_BANDS_2_COLOR = new Color(255, 255, 255, 255);
	private static final Color TRANSPARENT_PAINT = new Color(255, 255, 255, 0);

	private static final BasicStroke BID_ASK_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f);
	private static final BasicStroke MOV_AVG_STOKE = new BasicStroke(2);

	public static void main(String[] args) throws IOException {
		final CurrencyChartForm form = new CurrencyChartForm();
		form.inputDataChanged();
		final JFrame frame = new JFrame(String.format("Currency Watch %s", ManifestUtil.getManifestAttribute("currency-watch", "Implementation-Version")));
		frame.setContentPane(form.formPanel);
		form.exitButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			@Override public void windowClosed(WindowEvent e) {
				form.presenter.close();
			}
		});
	}

	public CurrencyChartForm() {
		presenter = new CurrencyRatePresenter(chartPanel.getChart().getXYPlot(), new FormCurrencyRatePresenterListener());
		currencyComboBox.setModel(new DefaultComboBoxModel<>(CurrencySymbol.values()));
		currencyComboBox.setSelectedItem(DEFAULT_CURRENCY);
		currencyComboBox.addActionListener(new InputDataListener());
		periodComboBox.setModel(new DefaultComboBoxModel<>(Period.values()));
		periodComboBox.setSelectedItem(DEFAULT_PERIOD);
		periodComboBox.addActionListener(new InputDataListener());
		qualityComboBox.setModel(new DefaultComboBoxModel<>(SeriesQuality.values()));
		qualityComboBox.setSelectedItem(DEFAULT_SERIES_QUALITY);
		qualityComboBox.addActionListener(new InputDataListener());
		bidAskCheckBox.addActionListener(new InputDataListener());
		movAvgCheckBox.addActionListener(new PeriodComboBoxEnabledInputDataListener());
		bollingerBandsCheckBox.addActionListener(new PeriodComboBoxEnabledInputDataListener());
		movAvgPeriodComboBox.setModel(new DefaultComboBoxModel<>(MOV_AVG_PERIODS));
		movAvgPeriodComboBox.setSelectedItem(DEFAULT_MOV_AVG_PERIOD);
		movAvgPeriodComboBox.addActionListener(new InputDataListener());
		formPanel.addComponentListener(new ComponentAdapter() {
			@Override public void componentResized(ComponentEvent e) {
				resizeChartPanel();
			}
		});
	}

	private void createUIComponents() {
		DateAxis xAxis = new DateAxis("Date");
		NumberAxis yAxis = new NumberAxis("Middle");
		yAxis.setAutoRangeIncludesZero(false);

		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
			StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
			new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")
		));
		renderer.setSeriesPaint(0, MIDDLE_COLOR);

		XYAreaRenderer2 bbRenderer = new XYAreaRenderer2();
		bbRenderer.setSeriesPaint(0, TRANSPARENT_PAINT);
		bbRenderer.setSeriesPaint(1, BOLL_BANDS_2_COLOR);
		bbRenderer.setSeriesPaint(2, BOLL_BANDS_COLOR);
		bbRenderer.setOutline(false);

		XYPlot plot = new XYPlot(null, xAxis, yAxis, renderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setRenderer(1, bbRenderer);

		JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(SystemColor.control);
		chartPanel = new ChartPanel(chart);
	}

	private void inputDataChanged() {
		boolean showBidAsk = bidAskCheckBox.isSelected();
		boolean showMovAvg = movAvgCheckBox.isSelected();
		presenter.inputDataChanged(
			(CurrencySymbol)currencyComboBox.getSelectedItem(),
			(Period)periodComboBox.getSelectedItem(),
			(SeriesQuality)qualityComboBox.getSelectedItem(),
			showBidAsk,
			showMovAvg,
			bollingerBandsCheckBox.isSelected(),
			(Integer)movAvgPeriodComboBox.getSelectedItem()
		);
		if (showBidAsk || showMovAvg) {
			XYItemRenderer renderer = chartPanel.getChart().getXYPlot().getRenderer();
			if (showBidAsk) {
				renderer.setSeriesPaint(1, BID_COLOR);
				renderer.setSeriesPaint(2, ASK_COLOR);
				renderer.setSeriesStroke(1, BID_ASK_STROKE);
				renderer.setSeriesStroke(2, BID_ASK_STROKE);
			}
			if (showMovAvg) {
				int movAvgIndex = showBidAsk ? 3 : 1;
				renderer.setSeriesPaint(movAvgIndex, MOV_AVG_COLOR);
				renderer.setSeriesStroke(movAvgIndex, MOV_AVG_STOKE);
			}
		}
	}

	private void setPeriodComboBoxEnabled() {
		movAvgPeriodComboBox.setEnabled(movAvgCheckBox.isSelected() || bollingerBandsCheckBox.isSelected());
	}

	private void resizeChartPanel() {
		Dimension size = new Dimension(
			formPanel.getWidth(),
			formPanel.getHeight() - toolPanel.getHeight() - statusPanel.getHeight() - 10
		);
		chartPanel.setSize(size);
		chartPanel.setPreferredSize(size);
		chartPanel.setMinimumSize(size);
		chartPanel.setMaximumSize(size);
	}

	private class FormCurrencyRatePresenterListener implements CurrencyRatePresenterListener {
		@Override public void statusChanged(String status, boolean isError) {
			statusLabel.setText(StringUtil.maxLength(status, 25));
			statusLabel.setForeground(isError ? Color.RED : Color.BLACK);
		}
		@Override public void progressChanged(int progress) {
			progressBar.setValue(progress);
		}
		@Override public void ratesPerSecChanged(double ratesPerSec) {
			speedLabel.setText(ratesPerSec > 0.0 ? String.format("%8.1f", ratesPerSec) + " rate/s" : StringUtil.EMPTY);
		}
	}

	private class InputDataListener implements ActionListener {
		@Override public void actionPerformed(ActionEvent e) {
			inputDataChanged();
		}
	}

	private class PeriodComboBoxEnabledInputDataListener implements ActionListener {
		@Override public void actionPerformed(ActionEvent e) {
			setPeriodComboBoxEnabled();
			inputDataChanged();
		}
	}
}
