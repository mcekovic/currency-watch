package test.strangeforest.currencywatch.unit.core;

import java.io.*;

import org.junit.*;
import org.strangeforest.currencywatch.core.*;

import static org.assertj.core.api.Assertions.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRatesKeyTest {

	@Test
	public void keyIsSerializedAndDeserialized() throws IOException, ClassNotFoundException {
		CurrencyRatesKey key = new CurrencyRatesKey(BASE_CURRENCY, CURRENCY);

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bOut);
		out.writeObject(key);

		byte[] buffer = bOut.toByteArray();
		assertThat(buffer.length).isLessThanOrEqualTo(150);

		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
		CurrencyRatesKey key2 = (CurrencyRatesKey)in.readObject();

		assertThat(key2).isEqualTo(key);
	}
}
