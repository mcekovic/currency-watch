package test.strangeforest.currencywatch.integration.rest;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import org.glassfish.jersey.server.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.rest.*;
import org.testng.annotations.*;

import com.sun.net.httpserver.*;

import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRateResourceFixture {

	private HttpServer httpServer;

	private static final int PORT = 8888;
	private static final String PATH = "/api";
	public static final java.net.URI URI = java.net.URI.create("http://localhost:" + PORT + PATH);

	@BeforeSuite
	public void start() throws IOException {
		httpServer = createHTTPServer();
		registerResource(httpServer, createResource());
		httpServer.start();
	}

	private static HttpServer createHTTPServer() throws IOException {
		HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), -1);
		httpServer.setExecutor(Executors.newCachedThreadPool());
		return httpServer;
	}

	private static CurrencyRateResource createResource() {
		CurrencyRateCache rateCache = new CurrencyRateCache();
		rateCache.setRate(BASE_CURRENCY, CURRENCY, DATE, RATE);
		rateCache.setRates(BASE_CURRENCY, CURRENCY, RATES);
		return new CurrencyRateResource(rateCache);
	}

	private static void registerResource(HttpServer httpServer, CurrencyRateResource resource) {
		ResourceConfig resourceConfig = new ResourceConfig();
		resourceConfig.register(resource);
		httpServer.createContext(PATH, ContainerFactory.createContainer(HttpHandler.class, resourceConfig));
	}

	@AfterSuite
	public void stop() {
		httpServer.stop(0);
	}
}
