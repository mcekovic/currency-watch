package test.strangeforest.currencywatch.integration.system;

import java.io.*;

import org.strangeforest.currencywatch.cmd.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.*;

import static org.testng.Assert.*;

public class CurrencyRateFetcherIT {

	private static final String MAPDB_PATH_NAME = "target/data/test-rates-fetcher-db";

	@BeforeClass
	private void setUp() throws IOException {
		ITUtil.ensurePath(MAPDB_PATH_NAME);
		ITUtil.deleteFile(MAPDB_PATH_NAME);
	}

	@Test
	public void showUsage() {
		CurrencyRateFetcher.main(new String[] {"-h"});
		assertFalse(new File(MAPDB_PATH_NAME).exists());
	}

	@Test(dependsOnMethods = "showUsage")
	public void invalidArguments() {
		CurrencyRateFetcher.main(new String[] {"-i"});
		CurrencyRateFetcher.main(new String[] {"-from", "32-15-2005"});
		assertFalse(new File(MAPDB_PATH_NAME).exists());
	}

	@Test(dependsOnMethods = "invalidArguments")
	public void fetchRates() {
		CurrencyRateFetcher.main(new String[] {"-db", MAPDB_PATH_NAME,"-from", "01-01-2012", "-to", "02-01-2012", "-t", "2"});
		assertTrue(new File(MAPDB_PATH_NAME).exists());
	}
}
