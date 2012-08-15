package org.strangeforest.currencywatch.web.rest;

import java.util.*;
import javax.xml.bind.annotation.*;

import org.strangeforest.currencywatch.core.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rate", propOrder = {"date", "bid", "ask", "middle"}, namespace = "http://currencywatch.strangeforest.org")
public class RateObject {

	@XmlAttribute(required=true) Date date;
	@XmlAttribute(required=true) private double bid;
	@XmlAttribute(required=true) private double ask;
	@XmlAttribute(required=true) private double middle;

	public RateObject() {}

	public RateObject(Date date, RateValue rate) {
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

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public double getAsk() {
		return ask;
	}

	public void setAsk(double ask) {
		this.ask = ask;
	}

	public double getMiddle() {
		return middle;
	}

	public void setMiddle(double middle) {
		this.middle = middle;
	}
}
