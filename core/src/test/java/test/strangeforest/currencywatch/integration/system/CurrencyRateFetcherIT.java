package test.strangeforest.currencywatch.integration.system;

import java.io.*;

import org.strangeforest.currencywatch.cmd.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.*;

import static org.testng.Assert.*;

public class CurrencyRateFetcherIT {

	private static final String DB4O_DATA_FILE = "data/test-rates-fetcher.db4o";

	@BeforeClass
	private void setUp() {
		ITUtil.deleteFile(DB4O_DATA_FILE);
	}

	@Test
	public void showUsage() {
		CurrencyRateFetcher.main(new String[] {"-h"});
		assertFalse(new File(DB4O_DATA_FILE).exists());
	}

	@Test(dependsOnMethods = "showUsage")
	public void invalidArguments() {
		CurrencyRateFetcher.main(new String[] {"-i"});
		CurrencyRateFetcher.main(new String[] {"-from", "32-15-2005"});
		assertFalse(new File(DB4O_DATA_FILE).exists());
	}

	@Test(dependsOnMethods = "invalidArguments")
	public void fetchRates() {
		CurrencyRateFetcher.main(new String[] {"-db", DB4O_DATA_FILE,"-from", "01-01-2012", "-to", "02-01-2012", "-t", "2"});
		assertTrue(new File(DB4O_DATA_FILE).exists());
	}
}
