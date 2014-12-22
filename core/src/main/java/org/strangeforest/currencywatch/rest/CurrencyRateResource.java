package org.strangeforest.currencywatch.rest;

import java.text.*;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.slf4j.*;
import org.springframework.stereotype.*;
import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.rest.model.*;
import org.strangeforest.util.*;

import static java.util.stream.Collectors.*;
import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

@Component
@Path("/")
public class CurrencyRateResource {

	private final CurrencyRateProvider provider;
	private String baseCurrency = Util.BASE_CURRENCY;

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	static final String DATE_SEPARATOR = ",";
	static final String INFO_MESSAGE = "Currency Watch REST API";

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyRateResource.class);

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
	public Response rate(
		@PathParam("currency") String currency,
		@QueryParam("date") String date
	) {
		try {
			Date aDate = parseDate(date);
			if (aDate == null)
				aDate = Util.toDate(Util.getLastDate());
			RateValue rate = provider.getRate(baseCurrency, currency, aDate);
			if (rate != null)
				return Response.ok(new RateType(aDate, rate), TEXT_XML).build();
		}
		catch (ParseException ex) {
			return Response.status(BAD_REQUEST).type(TEXT_PLAIN).entity(ex.getMessage()).build();
		}
		catch (Throwable th) {
			LOGGER.error("Error getting currency rate.", th);
			return Response.status(INTERNAL_SERVER_ERROR).type(TEXT_PLAIN).entity(th.getMessage()).build();
		}
		String message = String.format("Cannot find currency rate for currency %1$s and date %2$s", currency, date);
		return Response.status(NOT_FOUND).type(TEXT_PLAIN).entity(message).build();
	}

	@GET @Path("/rates/{currency}") @Produces(TEXT_XML)
	public Response rates(
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
					to = Util.toDate(Util.getLastDate());
				if (from == null)
					from = to;
				dateColl = Util.trimDateRange(new DateRange(from, to)).dates();
			}
			Map<Date, RateValue> rates = provider.getRates(baseCurrency, currency, dateColl);
			return Response.ok(new RatesType(rates.entrySet().stream().map(rate -> new RateType(rate.getKey(), rate.getValue())).collect(toList()))).build();
		}
		catch (ParseException ex) {
			return Response.status(BAD_REQUEST).type(TEXT_PLAIN).entity(ex.getMessage()).build();
		}
		catch (Throwable th) {
			LOGGER.error("Error getting currency rates.", th);
			return Response.status(INTERNAL_SERVER_ERROR).type(TEXT_PLAIN).entity(th.getMessage()).build();
		}
	}

	private static Date parseDate(String date) throws ParseException {
		return !StringUtil.isNullOrEmpty(date) ? DATE_FORMAT.parse(date) : null;
	}
}
