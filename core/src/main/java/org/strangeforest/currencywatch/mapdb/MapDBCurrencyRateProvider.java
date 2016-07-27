package org.strangeforest.currencywatch.mapdb;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.mapdb.*;
import org.slf4j.*;
import org.strangeforest.currencywatch.core.*;

public class MapDBCurrencyRateProvider implements UpdatableCurrencyRateProvider {

	private final String dbPathName;
	private final int version;
	private DB db;
	private ConcurrentMap<CurrencyRatesKey, CurrencyRatesValue> currencyRates;

	public static final int CURRENT_VERSION = 2;

	private static final String CURRENCY_RATES_MAP = "CurrencyRates";
	private static final String VERSION_MAP = "Version";

	private static final Logger LOGGER = LoggerFactory.getLogger(MapDBCurrencyRateProvider.class);

	public MapDBCurrencyRateProvider(String dbPathName) {
		this(dbPathName, CURRENT_VERSION);
	}
	public MapDBCurrencyRateProvider(String dbPathName, int version) {
		this.dbPathName = dbPathName;
		this.version = version;
	}

	@Override public void init() {
		db = createMapDB();
		Integer dbVersion = getVersionMap().get(VERSION_MAP);
		if (dbVersion == null || !dbVersion.equals(version)) {
			db.close();
			createMapDBForFilesDeletion().close();
			db = createMapDB();
			getVersionMap().put(VERSION_MAP, version);
			db.commit();
			LOGGER.info("MapDB cleared as data version is old.");
		}
		currencyRates = getCurrencyRatesMap();
		db.commit();
	}

	private DB createMapDB() {
		return DBMaker.fileDB(new File(dbPathName)).closeOnJvmShutdown().make();
	}

	private DB createMapDBForFilesDeletion() {
		return DBMaker.fileDB(new File(dbPathName)).deleteFilesAfterClose().make();
	}

	private HTreeMap<String, Integer> getVersionMap() {
		return db.hashMap(VERSION_MAP, Serializer.STRING, Serializer.INTEGER).createOrOpen();
	}

	private HTreeMap<CurrencyRatesKey, CurrencyRatesValue> getCurrencyRatesMap() {
		return db.hashMap(CURRENCY_RATES_MAP, Serializer.JAVA, Serializer.JAVA).createOrOpen();
	}

	@Override public void close() {
		try {
			db.commit();
		}
		finally {
			db.close();
		}
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		CurrencyRatesValue ratesValue = currencyRates.get(new CurrencyRatesKey(baseCurrency, currency));
		return ratesValue != null ? ratesValue.getRate(date) : null;
	}

	@Override public Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		CurrencyRatesValue ratesValue = currencyRates.get(new CurrencyRatesKey(baseCurrency, currency));
		return ratesValue != null ? ratesValue.getRates(dates) : Collections.emptyMap();
	}

	@Override public void setRate(String baseCurrency, String currency, Date date, RateValue rateValue) {
		CurrencyRatesKey key = new CurrencyRatesKey(baseCurrency, currency);
		currencyRates.compute(key, (k, rates) -> {
			Map<Date, RateValue> dateRates = rates != null ? new HashMap<>(rates.getRates()) : new HashMap<>();
			dateRates.put(date, rateValue);
			return new CurrencyRatesValue(baseCurrency, currency, dateRates);
		});
		db.commit();
	}

	@Override public void setRates(String baseCurrency, String currency, Map<Date, RateValue> dateRates) {
		CurrencyRatesKey key = new CurrencyRatesKey(baseCurrency, currency);
		currencyRates.compute(key, (k, rates) -> {
			Map<Date, RateValue> newDateRates = rates != null ? new HashMap<>(rates.getRates()) : new HashMap<>();
			newDateRates.putAll(dateRates);
			return new CurrencyRatesValue(baseCurrency, currency, newDateRates);
		});
		db.commit();
	}
}
