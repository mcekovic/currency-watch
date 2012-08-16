package org.strangeforest.currencywatch.rest;

import java.net.*;
import java.text.*;
import java.util.*;
import javax.ws.rs.core.*;

import org.strangeforest.currencywatch.core.*;

import com.sun.jersey.api.client.*;
import com.sun.jersey.client.apache.*;

import com.finsoft.util.*;

public class RESTCurrencyWatchProvider extends BaseCurrencyRateProvider {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	public static final char DATE_SEPARATOR = ',';

	private final URI uri;
	private Client client;
	private WebResource resource;

	public RESTCurrencyWatchProvider(URI uri) {
		super();
		this.uri = uri;
	}

	@Override public void init() {
		client = ApacheHttpClient.create();
		resource = client.resource(uri);
	}

	@Override public void close() {
		client.destroy();
	}

	public boolean ping() {
		try {
			return ObjectUtil.equal(resource.accept(MediaType.TEXT_PLAIN).get(String.class), "Currency Watch REST API");
		}
		catch (UniformInterfaceException | ClientHandlerException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		return toRateValue(resource.path("rate/" + currency).queryParam("date", formatDate(date))
				.accept(MediaType.TEXT_XML).get(RateType.class));
	}

	@Override public Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		return toRateValuesMap(resource.path("rates/" + currency).queryParam("dates", formatDates(dates))
				.accept(MediaType.TEXT_XML).get(RatesType.class));
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
		return new RateValue(rate.getBid(), rate.getAsk(), rate.getMiddle());
	}

	private Map<Date, RateValue> toRateValuesMap(RatesType rates) {
		Map<Date, RateValue> rateValueMap = new HashMap<>();
		for (RateType rate : rates.getRates())
			rateValueMap.put(rate.getDate(), toRateValue(rate));
		return rateValueMap;
	}
}
