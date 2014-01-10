package test.strangeforest.currencywatch.integration.rest;

import java.io.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

import org.strangeforest.currencywatch.rest.*;
import org.testng.annotations.*;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.strangeforest.currencywatch.rest.CurrencyRateResource.*;
import static org.testng.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRateResourceIT extends CurrencyRateResourceFixture {

	private Client client;
	private WebTarget target;

	@BeforeClass
	public void setUp() throws IOException {
		client = ClientBuilder.newClient();
		target = client.target(URI);
	}

	@AfterClass
	public void cleanUp() {
		if (client != null)
			client.close();
	}

	@Test
	public void getLastRate() {
		Response response = target.path("rate/" + CURRENCY).request(TEXT_XML).get();
		assertEquals(response.getStatus(), NO_CONTENT.getStatusCode());
	}

	@Test
	public void getLastRates() {
		RatesType ratesType = target.path("rates/" + CURRENCY).request(TEXT_XML).get(RatesType.class);
		assertNull(ratesType.getRates());
	}

	@Test
	public void getRatesForDateRange() {
		RatesType ratesType = target.path("rates/" + CURRENCY).queryParam("fromDate", DATE_FORMAT.format(DATE2)).queryParam("toDate", DATE_FORMAT.format(DATE4))
			.request(TEXT_XML).get(RatesType.class);
		assertEquals(ratesType.getRates().size(), 3);
	}

	@Test
	public void wrongDateFormat() {
		Response response = target.path("rate/" + CURRENCY).queryParam("date", "bad-date").request(TEXT_XML).get();
		assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
		Response response2 = target.path("rates/" + CURRENCY).queryParam("fromDate", "bad-date").request(TEXT_XML).get();
		assertEquals(response2.getStatus(), BAD_REQUEST.getStatusCode());
	}
}
