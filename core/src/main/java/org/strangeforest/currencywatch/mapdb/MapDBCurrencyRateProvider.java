package org.strangeforest.currencywatch.mapdb;

import java.io.*;
import java.util.*;

import org.mapdb.*;
import org.slf4j.*;
import org.strangeforest.currencywatch.core.*;

public class MapDBCurrencyRateProvider implements UpdatableCurrencyRateProvider {

	private final String dbPathName;
	private final int version;
	private DB db;
	private HTreeMap<CurrencyRatesKey, CurrencyRatesValue> currencyRates;

	private static final String CURRENCY_RATES_MAP = "CurrencyRates";
	private static final String VERSION_MAP = "Version";
	private static final int CURRENT_VERSION = 1;

	private static final Logger LOGGER = LoggerFactory.getLogger(MapDBCurrencyRateProvider.class);

	public MapDBCurrencyRateProvider(String dbPathName) {
		this(dbPathName, CURRENT_VERSION);
	}
	public MapDBCurrencyRateProvider(String dbPathName, int version) {
		this.dbPathName = dbPathName;
		this.version = version;
	}

	@Override public void init() {
		db = DBMaker.newFileDB(new File(dbPathName)).closeOnJvmShutdown().make();
		currencyRates = db.getHashMap(CURRENCY_RATES_MAP);
		Map<String, Integer> versionMap = db.<String, Integer>getHashMap(VERSION_MAP);
		Integer dbVersion = versionMap.get(VERSION_MAP);
		if (dbVersion != null && !dbVersion.equals(version)) {
			currencyRates.clear();
			LOGGER.info("MapDB cleared as data version is old.");
		}
		versionMap.put(VERSION_MAP, version);
	}

	@Override public void close() {
		db.close();
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		CurrencyRatesValue ratesValue = currencyRates.get(new CurrencyRatesKey(baseCurrency, currency));
		return ratesValue != null ? ratesValue.getRate(date) : null;
	}

	@Override public Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		CurrencyRatesValue ratesValue = currencyRates.get(new CurrencyRatesKey(baseCurrency, currency));
		return ratesValue != null ? new TreeMap<>(ratesValue.getRates(dates)) : Collections.emptyMap();
	}

	@Override public void setRate(String baseCurrency, String currency, Date date, RateValue rateValue) {
		CurrencyRatesKey key = new CurrencyRatesKey(baseCurrency, currency);
		currencyRates.compute(key, (k, rates) -> {
			Map<Date, RateValue> dateRates = rates != null ? new HashMap<>(rates.getRates()) : new HashMap<>();
			dateRates.put(date, rateValue);
			return new CurrencyRatesValue(baseCurrency, currency, dateRates);
		});
	}

	@Override public void setRates(String baseCurrency, String currency, Map<Date, RateValue> dateRates) {
		CurrencyRatesKey key = new CurrencyRatesKey(baseCurrency, currency);
		currencyRates.compute(key, (k, rates) -> {
			Map<Date, RateValue> newDateRates = rates != null ? new HashMap<>(rates.getRates()) : new HashMap<>();
			newDateRates.putAll(dateRates);
			return new CurrencyRatesValue(baseCurrency, currency, newDateRates);
		});
	}
}
