package org.strangeforest.currencywatch.web.rest;

import org.glassfish.jersey.server.*;
import org.strangeforest.currencywatch.rest.*;

public class RESTConfig extends ResourceConfig {

	public RESTConfig() {
		register(CurrencyRateResource.class);
	}
}
