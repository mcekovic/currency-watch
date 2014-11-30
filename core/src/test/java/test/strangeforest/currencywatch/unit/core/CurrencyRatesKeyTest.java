package test.strangeforest.currencywatch.unit.core;

import java.io.*;

import org.junit.*;
import org.strangeforest.currencywatch.core.*;

import static org.junit.Assert.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRatesKeyTest {

	@Test
	public void keyIsSerializedAndDeserialized() throws IOException, ClassNotFoundException {
		CurrencyRatesKey key = new CurrencyRatesKey(BASE_CURRENCY, CURRENCY);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(buffer);
		out.writeObject(key);

		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
		CurrencyRatesKey key2 = (CurrencyRatesKey)in.readObject();

		assertEquals("Keys do not match.", key, key2);
	}

}
