package test.strangeforest.currencywatch.unit.core;

import org.junit.*;
import org.mockito.*;
import org.strangeforest.currencywatch.core.*;

import static org.mockito.Mockito.*;
import static test.strangeforest.currencywatch.TestData.*;

public class TracingCurrencyRateProviderProxyTest {

	@Test
	public void allMethodsAreProxied() {
		CurrencyRateProvider provider = mock(CurrencyRateProvider.class);

		TracingCurrencyRateProviderProxy proxy = new TracingCurrencyRateProviderProxy(provider);
		proxy.getRate(BASE_CURRENCY, CURRENCY, DATE);
		proxy.getRates(BASE_CURRENCY, CURRENCY, DATES);

		InOrder order = inOrder(provider);
		order.verify(provider).getRate(BASE_CURRENCY, CURRENCY, DATE);
		order.verify(provider).getRates(BASE_CURRENCY, CURRENCY, DATES);
	}
}
