package test.strangeforest.currencywatch.integration;

import java.io.*;

public abstract class ITUtil {

	public static void deleteFile(String name) {
		File file = new File(name);
		file.delete();
		file.deleteOnExit();
	}
}
