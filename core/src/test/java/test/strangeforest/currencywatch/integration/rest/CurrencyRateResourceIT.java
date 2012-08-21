package test.strangeforest.currencywatch.integration.rest;

import java.io.*;

import org.strangeforest.currencywatch.rest.*;
import org.testng.annotations.*;

import com.sun.jersey.api.client.*;
import com.sun.jersey.client.apache.*;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.strangeforest.currencywatch.rest.CurrencyRateResource.DATE_FORMAT;
import static org.testng.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRateResourceIT {

	private ApacheHttpClient httpClient;
	private WebResource resource;

	@BeforeClass
	public void setUp() throws IOException {
		httpClient = ApacheHttpClient.create();
		resource = httpClient.resource(CurrencyRateResourceFixture.URI);
	}

	@AfterClass
	public void shutDown() {
		httpClient.destroy();
	}

	@Test
	public void getLastRate() {
		ClientResponse response = resource.path("rate/" + CURRENCY).accept(TEXT_XML).get(ClientResponse.class);
		assertEquals(response.getStatus(), NO_CONTENT.getStatusCode());
	}

	@Test
	public void getRatesForDateRange() {
		RatesType ratesType = resource.path("rates/" + CURRENCY).queryParam("fromDate", DATE_FORMAT.format(DATE2)).queryParam("toDate", DATE_FORMAT.format(DATE4))
			.accept(TEXT_XML).get(RatesType.class);
		assertEquals(ratesType.getRates().size(), 3);
	}

	@Test
	public void wrongDateFormat() {
		ClientResponse response = resource.path("rate/" + CURRENCY).queryParam("date", "bad-date").accept(TEXT_XML).get(ClientResponse.class);
		assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
		ClientResponse response2 = resource.path("rates/" + CURRENCY).queryParam("fromDate", "bad-date").accept(TEXT_XML).get(ClientResponse.class);
		assertEquals(response2.getStatus(), BAD_REQUEST.getStatusCode());
	}
}
