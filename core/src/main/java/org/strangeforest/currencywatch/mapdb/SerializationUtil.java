package org.strangeforest.currencywatch.mapdb;

import java.io.*;
import java.math.*;

public class SerializationUtil {

	// Variable Integer

	private static final int MASK = ~(1 << 7) & 0xFF;

	public static void writeInt(OutputStream out, int i) throws IOException {
		int b = 0;
		int bit = 0;
		do {
			if ((i & 1) != 0)
				b |= 1 << bit;
			i >>>= 1;
			bit++;
			if (bit == 7) {
				b |= (1 << 7);
				out.write(b & 0xff);
				b = 0;
				bit = 0;
			}
		}
		while (i > 0);
		out.write(b & 0xff);
	}

	public static int readInt(InputStream in) throws IOException {
		int i = 0;
		int b;
		int bIndex = 0;
		do {
			b = in.read();
			if (b == -1)
				throw new IOException("EOF reached while reading int value.");
			b &= 0xff;
			int bc = bIndex * 7;
			i |= ((b & MASK) << bc);
			bIndex++;
		}
		while ((b & (1<<7)) != 0);
		return i;
	}


	// BigDecimal

	public static void writeDecimal(ObjectOutputStream out, BigDecimal d) throws IOException {
		byte[] unscaledValue = d.unscaledValue().toByteArray();
		writeInt(out, unscaledValue.length);
		out.write(unscaledValue);
		writeInt(out, d.scale());
	}

	public static BigDecimal readDecimal(ObjectInputStream in) throws IOException {
		int length = readInt(in);
		byte[] unscaledValue = new byte[length];
		int offset = 0;
		while (length > 0) {
			int readBytes = in.read(unscaledValue, offset, length);
			if (readBytes == 0)
				throw new IOException(String.format("Cannot fully read unscaled BigDecimal value, expecting %1$d but got %2$d bytes.", unscaledValue.length, offset));
			offset += readBytes;
			length -= readBytes;
		}
		return new BigDecimal(new BigInteger(unscaledValue), readInt(in));
	}
}
