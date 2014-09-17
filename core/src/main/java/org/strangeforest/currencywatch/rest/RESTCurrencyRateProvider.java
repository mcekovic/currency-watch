package org.strangeforest.currencywatch.rest;

import java.net.*;
import java.util.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

import org.slf4j.*;
import org.strangeforest.currencywatch.core.*;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.strangeforest.currencywatch.rest.CurrencyRateResource.*;

public class RESTCurrencyRateProvider implements CurrencyRateProvider {

	private final URI uri;
	private Client client;
	private WebTarget target;

	private static final Logger LOGGER = LoggerFactory.getLogger(RESTCurrencyRateProvider.class);

	public RESTCurrencyRateProvider(URI uri) {
		super();
		this.uri = uri;
	}

	@Override public void init() {
		client = ClientBuilder.newClient();
		target = client.target(uri);
	}

	@Override public void close() {
		if (client != null)
			client.close();
	}

	public boolean ping() {
		try {
			return Objects.equals(target.request(TEXT_PLAIN).get(String.class), INFO_MESSAGE);
		}
		catch (Exception ex) {
			LOGGER.debug("Error pinging REST Currency Watch API.", ex);
			return false;
		}
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		Response response = target.path("rate/" + currency).queryParam("date", formatDate(date)).request(TEXT_XML).get(Response.class);
		int status = response.getStatus();
		if (status == OK.getStatusCode())
			return toRateValue(response.readEntity(RateType.class));
		else if (status == NOT_FOUND.getStatusCode())
			return null;
		else
			throw toException(response);
	}

	@Override public Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		Response response = target.path("rates/" + currency).queryParam("dates", formatDates(dates)).request(TEXT_XML).get(Response.class);
		if (response.getStatus() == OK.getStatusCode())
			return toRateValuesMap(response.readEntity(RatesType.class));
		else
			throw toException(response);
	}

	private String formatDate(Date date) {
		return DATE_FORMAT.format(date);
	}

	private String formatDates(Collection<Date> dates) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Date date : dates) {
			if (first)
				first = false;
			else
				sb.append(DATE_SEPARATOR);
			sb.append(formatDate(date));
		}
		return sb.toString();
	}

	private RateValue toRateValue(RateType rate) {
		return new RateValue(rate.bid, rate.ask, rate.middle);
	}

	private Map<Date, RateValue> toRateValuesMap(RatesType rates) {
		Map<Date, RateValue> rateValueMap = new HashMap<>();
		List<RateType> rateTypes = rates.rates;
		if (rateTypes != null) {
			for (RateType rate : rateTypes)
				rateValueMap.put(rate.date, toRateValue(rate));
		}
		return rateValueMap;
	}

	private CurrencyRateException toException(Response response) {
		String message = response.hasEntity() ? response.readEntity(String.class) : String.format("HTTP error %1$d (%2$s).", response.getStatus(), response.getStatusInfo());
		throw new CurrencyRateException(message);
	}
}
