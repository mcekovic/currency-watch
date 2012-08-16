package org.strangeforest.currencywatch.web.rest;

import java.util.*;
import javax.xml.bind.annotation.*;

@XmlRootElement(name = "rates")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rates", propOrder = {"rates"}, namespace = "http://currencywatch.strangeforest.org")
public class RatesType {

	@XmlElement(required=true, name = "rate") List<RateType> rates;

	public RatesType() {}

	public RatesType(List<RateType> rates) {
		this.rates = rates;
	}

	public List<RateType> getRates() {
		return rates;
	}

	public void setRates(List<RateType> rates) {
		this.rates = rates;
	}
}
