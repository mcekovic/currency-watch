package test.strangeforest.currencywatch.integration;

import java.io.*;

public abstract class ITUtil {

	public static void deleteFile(String name) {
		File file = new File(name);
		file.delete();
		file.deleteOnExit();
	}

	public static void deleteFiles(String dir, final String pattern) {
		for (File file : new File(dir).listFiles(new FileFilter() {
			@Override public boolean accept(File file) {
				return file.getPath().matches(".*" + pattern);
			}
		})) {
			file.delete();
			file.deleteOnExit();
		}
	}
}
