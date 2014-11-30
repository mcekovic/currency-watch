package test.strangeforest.currencywatch.integration;

import java.io.*;
import java.nio.file.*;

public abstract class ITUtil {

	public static void ensureDirectory(String path) throws IOException {
		Files.createDirectories(Paths.get(path));
	}

	public static void ensurePath(String path) throws IOException {
		Files.createDirectories(Paths.get(path).getParent());
	}

	public static void deleteFile(String name) {
		File file = new File(name);
		file.delete();
		file.deleteOnExit();
	}

	public static void deleteFiles(String dir, final String pattern) {
		for (File file : new File(dir).listFiles(f -> f.getPath().matches(".*" + pattern))) {
			file.delete();
			file.deleteOnExit();
		}
	}
}
