package org.strangeforest.currencywatch.rest;

import java.text.*;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;

import com.finsoft.util.*;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

@Path("/")
public class CurrencyRateResource {

	private final CurrencyRateProvider provider;
	private String baseCurrency = Util.BASE_CURRENCY;

	static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	static final String DATE_SEPARATOR = ",";
	static final String INFO_MESSAGE = "Currency Watch REST API";

	public CurrencyRateResource(CurrencyRateProvider provider) {
		super();
		this.provider = provider;
	}

	public void setBaseCurrency(String baseCurrency) {
		this.baseCurrency = baseCurrency;
	}

	@GET @Produces(TEXT_PLAIN)
	public String info() {
		return INFO_MESSAGE;
	}

	@GET @Path("/rate/{currency}") @Produces(TEXT_XML)
	public RateType rate(
		@PathParam("currency") String currency,
		@QueryParam("date") String date
	) {
		try {
			Date aDate = parseDate(date);
			if (aDate == null)
				aDate = Util.getLastDate().getTime();
			return new RateType(aDate, provider.getRate(baseCurrency, currency, aDate));
		}
		catch (ParseException pEx) {
			throw new WebApplicationException(Response.status(BAD_REQUEST).entity(pEx.getMessage()).type(TEXT_PLAIN).build());
		}
		catch (Exception ex) {
			throw new WebApplicationException(ex, Response.status(INTERNAL_SERVER_ERROR).entity(ex.getMessage()).type(TEXT_PLAIN).build());
		}
	}

	@GET @Path("/rates/{currency}") @Produces(TEXT_XML)
	public RatesType rates(
		@PathParam("currency") String currency,
		@QueryParam("dates") String dates,
		@QueryParam("fromDate") String fromDate,
		@QueryParam("toDate") String toDate
	) {
		try {
			Collection<Date> dateColl;
			if (dates != null) {
				dateColl = new ArrayList<>();
				for (StringTokenizer tokenizer = new StringTokenizer(dates, DATE_SEPARATOR); tokenizer.hasMoreTokens(); )
					dateColl.add(parseDate(tokenizer.nextToken()));
			}
			else {
				Date from = parseDate(fromDate);
				Date to = parseDate(toDate);
				if (to == null)
					to = Util.getLastDate().getTime();
				if (from == null)
					from = to;
				dateColl = new DateRange(from, to).dates();
			}
			Map<Date, RateValue> rates = provider.getRates(baseCurrency, currency, dateColl);
			return new RatesType(Algorithms.transform(rates.entrySet(), new Transformer<Map.Entry<Date, RateValue>, RateType>() {
				@Override public RateType transform(Map.Entry<Date, RateValue> rate) {
					return new RateType(rate.getKey(), rate.getValue());
				}
			}));
		}
		catch (ParseException pEx) {
			throw new WebApplicationException(Response.status(BAD_REQUEST).entity(pEx.getMessage()).type(TEXT_PLAIN).build());
		}
		catch (Exception ex) {
			throw new WebApplicationException(ex, Response.status(INTERNAL_SERVER_ERROR).entity(ex.getMessage()).type(TEXT_PLAIN).build());
		}
	}

	private static Date parseDate(String date) throws ParseException {
		return !StringUtil.isNullOrEmpty(date) ? DATE_FORMAT.parse(date) : null;
	}
}
