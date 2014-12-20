package org.strangeforest.currencywatch.nbs;

import java.io.*;
import java.math.*;
import java.net.*;
import java.time.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;

import static java.math.BigDecimal.*;
import static org.strangeforest.util.StringUtil.*;

//TODO: Use https://webservices.nbs.rs/CommunicationOfficeService1_0/ExchangeRateXmlService.asmx?WSDL
public class NBSCurrencyRateProvider extends BaseObservableCurrencyRateProvider {

	private String url = NBS_URL;
	private Duration sessionTimeout = SESSION_TIMEOUT;
	private NBSFormat format = FORMAT;

	private final NBSSession session;

	private static final String NBS_URL = "http://www.nbs.rs/kursnaListaModul/naZeljeniDan.faces";
	private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);
	private static final NBSFormat FORMAT = NBSFormat.CSV;
	private static final int MAX_RESPONSE_LOG_LENGTH = 100;

	public NBSCurrencyRateProvider() {
		session = new NBSSession();
	}

	public NBSCurrencyRateProvider(CurrencyRateListener listener) {
		this();
		addListener(listener);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setSessionTimeout(Duration sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public void setFormat(NBSFormat format) {
		this.format = format;
	}

	@Override public void close() {
		session.close();
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		try {
			if (!session.isValid(sessionTimeout))
				session.open(url);
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
		URLConnection conn = new URL(url).openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		URLConnectionUtil.disableCaching(conn);
		conn.setRequestProperty("Cookie", "JSESSIONID=" + session.getSessionId());

		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()))) {
			out.print("index%3AbrKursneListe=&index%3Ayear=2012");
			out.printf("&index%%3AinputCalendar1=%1$td.%1$tm.%1$tY.", date);
			out.printf("&index%%3Avrsta=%1$d&index%%3Aprikaz=%2$d&index%%3AbuttonShow=Prika%%C5%%BEi", 1, format.index());
			out.printf("&com.sun.faces.VIEW=%1$s", session.getViewId());
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

	private RateValue findRateASCII(BufferedReader reader, String currency) throws IOException {
		String line;
		StringBuilder sb = new StringBuilder(500);
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			String[] fields = line.split(";");
			if (fields.length >= 9 && currency.equals(fields[4])) {
				int unit = Integer.parseInt(fields[5]);
				BigDecimal bid = new BigDecimal(fields[8]).divide(valueOf(unit), RoundingMode.HALF_EVEN);
				BigDecimal middle = new BigDecimal(fields[9]).divide(valueOf(unit), RoundingMode.HALF_EVEN);
				BigDecimal ask = new BigDecimal(fields[10]).divide(valueOf(unit), RoundingMode.HALF_EVEN);
				return new RateValue(bid, ask, middle);
			}
		}
		String response = sb.toString();
		if (response.equals("null13"))
			throw new CurrencyRateException(true);
		else
			throw new CurrencyRateException("Cannot find rate for " + currency + ". Invalid response: " + maxLength(response, MAX_RESPONSE_LOG_LENGTH));
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
					BigDecimal bid = new BigDecimal(fields[6]).divide(valueOf(unit), RoundingMode.HALF_EVEN);
					BigDecimal ask = new BigDecimal(fields[7]).divide(valueOf(unit), RoundingMode.HALF_EVEN);
					BigDecimal middle = bid.add(ask).divide(valueOf(2L), RoundingMode.HALF_EVEN);
					return new RateValue(bid, ask, middle);
				}
			}
		}
		if (!foundCSV) {
			String response = sb.toString();
			if (response.equals("null13"))
				throw new CurrencyRateException(true);
			else
				throw new CurrencyRateException("Invalid response: " + maxLength(response, MAX_RESPONSE_LOG_LENGTH));
		}
		else
			throw new CurrencyRateException("Cannot find rate for " + currency);
	}
}
