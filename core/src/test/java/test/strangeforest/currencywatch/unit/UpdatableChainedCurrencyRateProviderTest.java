package test.strangeforest.currencywatch.unit;

import org.junit.*;
import org.strangeforest.currencywatch.core.*;

import static org.mockito.Mockito.*;
import static test.strangeforest.currencywatch.TestData.*;

public class UpdatableChainedCurrencyRateProviderTest {

	@Test
	public void rateIsSetToBothProviders() {
		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		UpdatableCurrencyRateProvider remoteProvider = mock(UpdatableCurrencyRateProvider.class);

		try (UpdatableCurrencyRateProvider chainedProvider = createUpdatableChainedProvider(localProvider, remoteProvider)) {
			chainedProvider.setRate(BASE_CURRENCY, CURRENCY, DATE, RATE);

			verify(localProvider).setRate(BASE_CURRENCY, CURRENCY, DATE, RATE);
			verify(remoteProvider).setRate(BASE_CURRENCY, CURRENCY, DATE, RATE);
		}
	}

	@Test
	public void ratesAreSetToBothProviders() {
		UpdatableCurrencyRateProvider localProvider = mock(UpdatableCurrencyRateProvider.class);
		UpdatableCurrencyRateProvider remoteProvider = mock(UpdatableCurrencyRateProvider.class);

		try (UpdatableCurrencyRateProvider chainedProvider = createUpdatableChainedProvider(localProvider, remoteProvider)) {
			chainedProvider.setRates(BASE_CURRENCY, CURRENCY, RATES);

			verify(localProvider).setRates(BASE_CURRENCY, CURRENCY, RATES);
			verify(remoteProvider).setRates(BASE_CURRENCY, CURRENCY, RATES);
		}
	}

	private UpdatableCurrencyRateProvider createUpdatableChainedProvider(UpdatableCurrencyRateProvider localProvider, UpdatableCurrencyRateProvider remoteProvider) {
		UpdatableChainedCurrencyRateProvider chainedProvider = new UpdatableChainedCurrencyRateProvider(localProvider, remoteProvider);
		chainedProvider.init();
		return chainedProvider;
	}
}
