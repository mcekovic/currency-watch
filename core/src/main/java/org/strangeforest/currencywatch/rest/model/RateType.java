package org.strangeforest.currencywatch.rest.model;

import java.math.*;
import java.util.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.rest.*;

@XmlRootElement(name = "rate")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rate", propOrder = {"date", "bid", "ask", "middle"})
public class RateType {

	@XmlAttribute(required=true) @XmlSchemaType(name = "date") @XmlJavaTypeAdapter(XmlDateAdapter.class) public Date date;
	@XmlAttribute(required=true) public BigDecimal bid;
	@XmlAttribute(required=true) public BigDecimal ask;
	@XmlAttribute(required=true) public BigDecimal middle;

	public RateType() {}

	public RateType(Date date, RateValue rate) {
		this.date = date;
		this.bid = rate.getBid();
		this.ask = rate.getAsk();
		this.middle = rate.getMiddle();
	}
}
