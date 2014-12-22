package org.strangeforest.currencywatch.rest;

import java.text.*;
import java.util.*;
import javax.xml.bind.annotation.adapters.*;

public class XmlDateAdapter extends XmlAdapter<String, Date> {

	private static final String XML_DATE_FORMAT = "yyyy-MM-dd";

	@Override public Date unmarshal(String value) throws ParseException {
		return new SimpleDateFormat(XML_DATE_FORMAT).parse(value);
	}

	@Override public String marshal(Date value) {
		return new SimpleDateFormat(XML_DATE_FORMAT).format(value);
	}
}
