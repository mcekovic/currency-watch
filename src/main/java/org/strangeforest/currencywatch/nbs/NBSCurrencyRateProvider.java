package org.strangeforest.currencywatch.nbs;

import java.io.*;
import java.net.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;

//TODO: Use https://webservices.nbs.rs/CommunicationOfficeService1_0/ExchangeRateXmlService.asmx?WSDL
public class NBSCurrencyRateProvider extends BaseObservableCurrencyRateProvider {

	private String sessionId;
	private String viewId;

	private static final String NBS_URL = "http://www.nbs.rs/kursnaListaModul/naZeljeniDan.faces";

	@Override public void init() throws CurrencyRateException {
		try {
			URLConnection conn = new URL(NBS_URL + "?lang=lat").openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setUseCaches(false);
			conn.setRequestProperty("Pragma", "no-cache");
			conn.setRequestProperty("Cache-control", "no-cache");
			conn.connect();

			findSessionId(conn);
			findViewId(conn);
		}
		catch (Exception ex) {
			throw new CurrencyRateException("Error getting session ID.", ex);
		}
	}

	@Override public RateValue getRate(String symbolFrom, String symbolTo, Date date) throws CurrencyRateException {
		try {
			while (true) {
				try {
					return doGetRate(symbolFrom, symbolTo, date);
				}
				catch (CurrencyRateException ex) {
					if (!ex.isRecoverable())
						throw ex;
				}
			}
		}
		catch (Exception ex) {
			throw new CurrencyRateException("Error getting currency rate from %1$s tp %2$s for date: %3$s", ex, symbolFrom, symbolTo, date);
		}
	}

	private RateValue doGetRate(String symbolFrom, String symbolTo, Date date) throws IOException, CurrencyRateException {
		URLConnection conn = new URL(NBS_URL).openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Pragma", "no-cache");
		conn.setRequestProperty("Cache-control", "no-cache");
		if (sessionId != null)
			conn.setRequestProperty("Cookie", "JSESSIONID=" + sessionId);

		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()))) {
			out.print("index%3AbrKursneListe=&index%3Ayear=2010");
			out.printf("&index%%3AinputCalendar1=%1$td.%1$tm.%1$tY.", date);
			out.print("&index%3Avrsta=3&index%3Aprikaz=1&index%3AbuttonShow=Prika%C5%BEi");
			if (viewId != null)
				out.printf("&com.sun.faces.VIEW=%1$s", viewId);
			out.print("&index=index");
			out.flush();
		}

		conn.connect();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			return findRate(reader, symbolFrom, symbolTo, date);
		}
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

	private RateValue findRate(BufferedReader reader, String symbolFrom, String symbolTo, Date date) throws IOException, CurrencyRateException {
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
				if (fields.length >= 7 && symbolTo.equals(fields[4])) {
					double middle = Double.parseDouble(fields[6]) / Double.parseDouble(fields[5]);
					RateValue rateValue = new RateValue(middle, middle, middle);
					if (hasAnyListener())
						notifyListeners(symbolFrom, symbolTo, date, rateValue);
					return rateValue;
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
			throw new CurrencyRateException("Cannot find rate for " + symbolTo);
	}
}