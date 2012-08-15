package org.strangeforest.currencywatch.web.rest;

import java.text.*;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;

import com.finsoft.util.*;

@Path("/")
public class CurrencyRateResource {

	private CurrencyRateProvider provider;
	private String baseCurrency;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

	@GET @Produces(MediaType.TEXT_PLAIN)
	public String info() {
		return "Currency Watch REST API";
	}

	@GET @Path("/rates/{currency}") @Produces(MediaType.TEXT_XML)
	public List<RateObject> rates(
		@PathParam("currency") String currency,
		@QueryParam("fromDate") String fromDate,
		@QueryParam("fromDate") String toDate
	) {
		try {
			Date from = parseDate(fromDate);
			if (from == null)
				from = UIUtil.START_DATE.getTime();
			Date to = parseDate(toDate);
			if (to == null)
				to = UIUtil.getLastDate().getTime();
			Map<Date, RateValue> rates = provider.getRates(baseCurrency, currency, new DateRange(from, to).dates());
			return Algorithms.transform(rates.entrySet(), new Transformer<Map.Entry<Date, RateValue>, RateObject>() {
				@Override public RateObject transform(Map.Entry<Date, RateValue> rate) {
					return new RateObject(rate.getKey(), rate.getValue());
				}
			});
		}
		catch (ParseException pEx) {
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(pEx.getMessage()).type(MediaType.TEXT_PLAIN).build());
		}
		catch (Exception ex) {
			throw new WebApplicationException(ex, Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build());
		}
	}

	private static Date parseDate(String date) throws ParseException {
		return !StringUtil.isNullOrEmpty(date) ? DATE_FORMAT.parse(date) : null;
	}
}
