package test.strangeforest.currencywatch.unit.mapdb;

import java.io.*;

import org.junit.*;
import org.strangeforest.currencywatch.mapdb.*;

import static org.assertj.core.api.Assertions.*;
import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRatesValueTest {

	@Test
	public void valueIsSerializedAndDeserialized() throws IOException, ClassNotFoundException {
		CurrencyRatesValue value = new CurrencyRatesValue(BASE_CURRENCY, CURRENCY, RATES);

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bOut);
		out.writeObject(value);

		byte[] buffer = bOut.toByteArray();
		assertThat(buffer.length).isLessThanOrEqualTo(400);

		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
		CurrencyRatesValue value2 = (CurrencyRatesValue)in.readObject();

		assertThat(value2).isEqualTo(value);
	}
}
