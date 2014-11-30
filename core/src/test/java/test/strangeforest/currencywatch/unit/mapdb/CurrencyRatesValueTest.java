package test.strangeforest.currencywatch.unit.mapdb;

import java.io.*;

import org.junit.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.mapdb.*;

import static org.junit.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRatesValueTest {

	@Test
	public void valueIsSerializedAndDeserialized() throws IOException, ClassNotFoundException {
		CurrencyRatesValue value = new CurrencyRatesValue(BASE_CURRENCY, CURRENCY, RATES);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(buffer);
		out.writeObject(value);

		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
		CurrencyRatesValue value2 = (CurrencyRatesValue)in.readObject();

		assertEquals("Values do not match.", value, value2);
	}
}
