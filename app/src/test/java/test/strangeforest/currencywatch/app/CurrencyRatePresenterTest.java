package test.strangeforest.currencywatch.app;

import java.awt.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import javax.swing.*;

import org.hamcrest.*;
import org.junit.*;
import org.mockito.*;
import org.mockito.Matchers;
import org.strangeforest.currencywatch.app.*;
import org.strangeforest.currencywatch.core.*;
import org.strangeforest.currencywatch.ui.*;

import static java.util.function.Function.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyCollectionOf;
import static org.mockito.Mockito.*;
import static org.strangeforest.currencywatch.Util.*;

public class CurrencyRatePresenterTest {

	@Test
	public void currencyRatesArePresented() throws InterruptedException, InvocationTargetException {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);
		when(provider.getRates(eq(BASE_CURRENCY), eq(CurrencySymbol.EUR.name()), anyCollectionOf(Date.class))).thenAnswer(invocation -> {
         Collection<Date> dates = (Collection<Date>)invocation.getArguments()[2];
         return dates.stream().collect(Collectors.toMap(identity(), date -> new RateValue(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN)));
      });
		CurrencyRatePresenterListener listener = mock(CurrencyRatePresenterListener.class);

		CurrencyRatePresenter presenter = new CurrencyRatePresenter(provider);
		presenter.addListener(listener);

		presenter.inputDataChanged(CurrencySymbol.EUR, Period.WEEK, SeriesQuality.NORMAL, true, true, true, 2);

		presenter.waitForData();
		waitForEventQueueEmpty();

		verify(provider).getRates(eq(BASE_CURRENCY), eq(CurrencySymbol.EUR.name()), argThat(matchesSize(2)));
		verify(provider).getRates(eq(BASE_CURRENCY), eq(CurrencySymbol.EUR.name()), argThat(matchesSize(6)));
		verifyNoMoreInteractions(provider);

		verify(listener, atLeast(1)).statusChanged(any(String.class), eq(false));
		verify(listener, times(3)).progressChanged(any(Integer.class));
		verify(listener, times(2)).currentRate(Matchers.any(CurrentRate.class));
		verifyNoMoreInteractions(listener);

		presenter.removeListener(listener);
		presenter.close();
	}

	private static Matcher<Collection<Date>> matchesSize(int size) {
		return (Matcher)org.hamcrest.Matchers.hasSize(size);
	}

	private static void waitForEventQueueEmpty() throws InterruptedException, InvocationTargetException {
		while (Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent() != null)
			TimeUnit.MILLISECONDS.sleep(1L);
		SwingUtilities.invokeAndWait(() -> {});
	}
}
