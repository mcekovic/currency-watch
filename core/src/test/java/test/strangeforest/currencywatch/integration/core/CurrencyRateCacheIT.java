package test.strangeforest.currencywatch.integration.core;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import org.strangeforest.currencywatch.*;
import org.strangeforest.currencywatch.core.*;
import org.testng.*;
import org.testng.annotations.*;

import static test.strangeforest.currencywatch.TestData.*;

public class CurrencyRateCacheIT {

	private CurrencyRateCache cache;
	private Random rnd;
	private List<Date> dates;

	private static final int COUNT   = 100000;
	private static final int THREADS =    100;

	@BeforeClass
	public void setUp() {
		cache = new CurrencyRateCache();
		rnd = new Random();
		dates = new ArrayList<>(new DateRange(Util.toDate(Util.START_DATE), Util.toDate(Util.getLastDate())).dates());
	}

	@Test
	public void cachedValuesAreRetrievedConcurrently() throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		Future[] futures = new Future[COUNT];
		for (int i = 0; i < COUNT; i++) {
			futures[i] = executor.submit(() -> {
				try {
					setAndGetRate();
				}
				catch (InterruptedException ignored) {}
			});
		}
		for (Future future : futures)
			future.get();
	}

	private void setAndGetRate() throws InterruptedException {
		Date date = randomDate();
		RateValue rate = rateFromDate(date);
		cache.setRate(BASE_CURRENCY, CURRENCY, date, rate);
		TimeUnit.MILLISECONDS.sleep(1L);
		RateValue cachedRate = cache.getRate(BASE_CURRENCY, CURRENCY, date);
		Assert.assertEquals(cachedRate, rate);
	}

	private Date randomDate() {
		return dates.get(rnd.nextInt(dates.size()));
	}

	private RateValue rateFromDate(Date date) {
		LocalDate lDate = Util.toLocalDate(date);
		BigDecimal middle = new BigDecimal(100).add(new BigDecimal(lDate.getDayOfMonth()));
		BigDecimal spread = new BigDecimal(lDate.getDayOfWeek().ordinal() + 1).divide(BigDecimal.TEN).add(BigDecimal.ONE);
		return new RateValue(middle.subtract(spread), middle.add(spread), middle);
	}
}
