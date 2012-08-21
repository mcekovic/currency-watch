package test.strangeforest.currencywatch.integration.rest;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.rest.*;
import org.testng.annotations.*;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.container.*;
import com.sun.jersey.api.core.*;
import com.sun.jersey.client.apache.*;
import com.sun.net.httpserver.*;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.testng.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class RESTCurrencyRateProviderIT {

	private HttpServer httpServer;

	private static final int PORT = 8080;
	private static final String PATH = "/api";
	private static final URI URI = java.net.URI.create("http://localhost:" + PORT + PATH);

	private RESTCurrencyRateProvider provider;

	@BeforeClass
	public void setUp() throws IOException {
		httpServer = createHTTPServer();
		registerResource(httpServer, createResource());
		httpServer.start();

		provider = new RESTCurrencyRateProvider(URI);
		provider.init();
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
		DefaultResourceConfig resourceConfig = new DefaultResourceConfig();
		resourceConfig.getSingletons().add(resource);
		httpServer.createContext(PATH, ContainerFactory.createContainer(HttpHandler.class, resourceConfig));
	}

	@AfterClass
	public void shutDown() {
		provider.close();
		httpServer.stop(0);
	}

	@Test
	public void getRate() {
		RateValue rate = provider.getRate(BASE_CURRENCY, CURRENCY, DATE);
		assertEquals(rate, RATE);
	}

	@Test
	public void getRates() {
		Map<Date, RateValue> rates = provider.getRates(BASE_CURRENCY, CURRENCY, DATES);
		assertEquals(rates, RATES);
	}

	@Test
	public void unknownDate() {
		RateValue rate = provider.getRate(BASE_CURRENCY, CURRENCY, UNKNOWN_DATE);
		assertEquals(rate, null);
	}

	@Test
	public void wrongDateFormat() {
		ApacheHttpClient client = ApacheHttpClient.create();
		WebResource resource = client.resource(URI);
		ClientResponse response = resource.path("rate/" + CURRENCY).queryParam("date", "bad-date").accept(TEXT_XML).get(ClientResponse.class);
		assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
	}
}
