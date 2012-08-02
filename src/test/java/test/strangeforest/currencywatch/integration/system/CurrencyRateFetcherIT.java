package test.strangeforest.currencywatch.integration.system;

import java.io.*;

import org.strangeforest.currencywatch.cmd.*;
import org.testng.*;
import org.testng.annotations.*;

import test.strangeforest.currencywatch.integration.*;

public class CurrencyRateFetcherIT {

	private static final String DB4O_DATA_FILE = "data/test-rates-fetcher.db4o";

	@BeforeClass
	private void setUp() {
		ITUtil.deleteFile(DB4O_DATA_FILE);
	}

	@Test
	public void showUsage() {
		CurrencyRateFetcher.main(new String[] {"-h"});
		Assert.assertFalse(new File(DB4O_DATA_FILE).exists());
	}

	@Test(dependsOnMethods = "showUsage")
	public void fetchRates() {
		CurrencyRateFetcher.main(new String[] {"-db", DB4O_DATA_FILE,"-from", "01-01-2012", "-to", "02-01-2012", "-t", "2"});
		Assert.assertTrue(new File(DB4O_DATA_FILE).exists());
	}
}
