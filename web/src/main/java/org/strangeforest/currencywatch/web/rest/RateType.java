package org.strangeforest.currencywatch.web.rest;

import java.math.*;
import java.util.*;
import javax.xml.bind.annotation.*;

import org.strangeforest.currencywatch.core.*;

@XmlRootElement(name = "rate")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rate", propOrder = {"date", "bid", "ask", "middle"}, namespace = "http://currencywatch.strangeforest.org")
public class RateType {

	@XmlAttribute(required=true) Date date;
	@XmlAttribute(required=true) private BigDecimal bid;
	@XmlAttribute(required=true) private BigDecimal ask;
	@XmlAttribute(required=true) private BigDecimal middle;

	public RateType() {}

	public RateType(Date date, RateValue rate) {
		this.date = date;
		this.bid = rate.getBid();
		this.ask = rate.getAsk();
		this.middle = rate.getMiddle();
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	public BigDecimal getMiddle() {
		return middle;
	}

	public void setMiddle(BigDecimal middle) {
		this.middle = middle;
	}
}
