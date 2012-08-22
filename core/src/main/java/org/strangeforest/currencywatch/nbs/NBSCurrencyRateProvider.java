package org.strangeforest.currencywatch.nbs;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;

import static java.math.BigDecimal.*;

//TODO: Use https://webservices.nbs.rs/CommunicationOfficeService1_0/ExchangeRateXmlService.asmx?WSDL
public class NBSCurrencyRateProvider extends BaseObservableCurrencyRateProvider {

	private volatile String sessionId;
	private volatile String viewId;

	private static final String NBS_URL = "http://www.nbs.rs/kursnaListaModul/naZeljeniDan.faces";
	private static final Format FORMAT = Format.CSV;
	private Format format = FORMAT;

	public Format getFormat() {
		return format;
	}

	public void setFormat(Format format) {
		this.format = format;
	}

	public NBSCurrencyRateProvider() {}

	public NBSCurrencyRateProvider(CurrencyRateListener listener) {
		super();
		addListener(listener);
	}

	@Override public void init() {
		try {
			URLConnection conn = new URL(NBS_URL + "?lang=lat").openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			disableCaching(conn);
			conn.connect();

			findSessionId(conn);
			findViewId(conn);
		}
		catch (Exception ex) {
			throw new CurrencyRateException("Error getting session ID.", ex);
		}
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		try {
			while (true) {
				try {
					RateValue rateValue = doGetRate(currency, date);
					if (hasAnyListener())
						notifyListeners(baseCurrency, currency, date, rateValue);
					return rateValue;
				}
				catch (CurrencyRateException ex) {
					if (ex.isRecoverable())
						notifyListeners("Retrying...");
					else
						throw ex;
				}
			}
		}
		catch (Exception ex) {
			throw new CurrencyRateException("Error getting currency rate from %1$s to %2$s for date: %3$td-%3$tm-%3$tY", ex, baseCurrency, currency, date);
		}
	}

	private RateValue doGetRate(String currency, Date date) throws IOException {
		URLConnection conn = new URL(NBS_URL).openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		disableCaching(conn);
		if (sessionId != null)
			conn.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);

		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()))) {
			out.print("index%3AbrKursneListe=&index%3Ayear=2012");
			out.printf("&index%%3AinputCalendar1=%1$td.%1$tm.%1$tY.", date);
			out.printf("&index%%3Avrsta=%1$d&index%%3Aprikaz=%2$d&index%%3AbuttonShow=Prika%%C5%%BEi", 1, format.index());
			if (viewId != null)
				out.printf("&com.sun.faces.VIEW=%1$s", viewId);
			out.print("&index=index");
			out.flush();
		}

		conn.connect();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			switch(format) {
				case CSV:
					return findRateCSV(reader, currency);
				case ASCII:
					return findRateASCII(reader, currency);
				default:
					throw new IllegalStateException("Invalid NBS format: " + format);
			}
		}
	}

	private void disableCaching(URLConnection conn) {
		conn.setUseCaches(false);
		conn.setRequestProperty("Pragma", "no-cache");
		conn.setRequestProperty("Cache-control", "no-cache");
	}

	private void findSessionId(URLConnection conn) {
		String cookies = conn.getHeaderField("Set-Cookie");
		for (String cookie : cookies.split(";")) {
			int pos = cookie.indexOf('=');
			String name = cookie.substring(0, pos);
			String value = cookie.substring(pos+1);
			if (name.equals("JSESSIONID")) {
				sessionId = value;
				break;
			}
		}
	}

	private void findViewId(URLConnection conn) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				int pos = line.indexOf("id=\"com.sun.faces.VIEW\" value=\"");
				if (pos > 0) {
					viewId = line.substring(pos + 31, line.indexOf("\"", pos + 31));
					break;
				}
			}
		}
	}

	private RateValue findRateASCII(BufferedReader reader, String currency) throws IOException {
		String line;
		StringBuilder sb = new StringBuilder(500);
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			String[] fields = line.split(";");
			if (fields.length >= 9 && currency.equals(fields[4])) {
				int unit = Integer.parseInt(fields[5]);
				BigDecimal bid = new BigDecimal(fields[8]).divide(valueOf(unit));
				BigDecimal middle = new BigDecimal(fields[9]).divide(valueOf(unit));
				BigDecimal ask = new BigDecimal(fields[10]).divide(valueOf(unit));
				return new RateValue(bid, ask, middle);
			}
		}
		if (sb.toString().equals("null13"))
			throw new CurrencyRateException(true);
		else
			throw new CurrencyRateException("Cannot find rate for " + currency + ". Invalid response: " + sb.toString());
	}

	private RateValue findRateCSV(BufferedReader reader, String currency) throws IOException {
		String line;
		boolean foundCSV = false;
		StringBuilder sb = new StringBuilder(500);
		while ((line = reader.readLine()) != null) {
			if (!foundCSV) {
				foundCSV = line.trim().startsWith("BrojKursneListe");
				if (!foundCSV) {
					sb.append(line);
					sb.append(Character.LINE_SEPARATOR);
				}
			}
			else {
				if (line.charAt(0) == ' ')
					break;
				String[] fields = line.split(",");
				if (fields.length >= 7 && currency.equals(fields[4])) {
					int unit = Integer.parseInt(fields[5]);
					BigDecimal bid = new BigDecimal(fields[6]).divide(valueOf(unit));
					BigDecimal ask = new BigDecimal(fields[7]).divide(valueOf(unit));
					BigDecimal middle = bid.add(ask).divide(valueOf(2L));
					return new RateValue(bid, ask, middle);
				}
			}
		}
		if (!foundCSV) {
			if (sb.toString().equals("null13"))
				throw new CurrencyRateException(true);
			else
				throw new CurrencyRateException("Invalid response: " + sb.toString());
		}
		else
			throw new CurrencyRateException("Cannot find rate for " + currency);
	}

	public enum Format {
		CSV(1), ASCII(5);

		private int index;

		private Format(int index) {
			this.index = index;
		}

		public int index() {
			return index;
		}
	}
}
