package org.strangeforest.currencywatch.nbs;

import java.io.*;
import java.net.*;
import java.time.*;

import org.slf4j.*;
import org.strangeforest.currencywatch.core.*;

public class NBSSession {

	private volatile String sessionId;
	private volatile String viewId;
	private volatile LocalTime sessionStart;

	private static final Logger LOGGER = LoggerFactory.getLogger(NBSSession.class);

	public boolean isValid(Duration sessionTimeout) {
		return sessionId == null || viewId == null || sessionStart == null || sessionStart.plus(sessionTimeout).isBefore(LocalTime.now());
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getViewId() {
		return viewId;
	}

	public void open(String url) {
		try {
			URLConnection conn = new URL(url + "?lang=lat").openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			URLConnectionUtil.disableCaching(conn);
			conn.connect();

			findSessionId(conn);
			findViewId(conn);
			if (sessionId != null && viewId != null)
				sessionStart = LocalTime.now();
		}
		catch (Exception ex) {
			throw new CurrencyRateException("Error creating NBS session.", ex);
		}
	}

	public void close() {
		sessionId = null;
		viewId = null;
		sessionStart = null;
	}

	private void findSessionId(URLConnection conn) {
		String cookies = conn.getHeaderField("Set-Cookie");
		if (cookies != null) {
			for (String cookie : cookies.split(";")) {
				int pos = cookie.indexOf('=');
				String name = cookie.substring(0, pos);
				String value = cookie.substring(pos + 1);
				if (name.equals("JSESSIONID")) {
					sessionId = value;
					break;
				}
			}
		}
		if (sessionId == null)
			LOGGER.error("Cannot find session ID cookie.");
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
		if (viewId == null)
			LOGGER.error("Cannot find JSF view ID.");
	}
}
