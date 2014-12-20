package org.strangeforest.currencywatch.nbs;

import java.net.*;

public class URLConnectionUtil {

	public static void disableCaching(URLConnection conn) {
		conn.setUseCaches(false);
		conn.setRequestProperty("Pragma", "no-cache");
		conn.setRequestProperty("Cache-control", "no-cache");
	}
}
