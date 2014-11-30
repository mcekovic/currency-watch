package org.strangeforest.currencywatch.db4o;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.slf4j.*;
import org.strangeforest.currencywatch.core.*;

import com.db4o.*;
import com.db4o.config.*;

public class Db4oCurrencyRateProvider implements UpdatableCurrencyRateProvider {

	private final String dbFileName;
	private final int dataVersion;
	private ObjectContainer db;
	private boolean closed;

	private static final Logger LOGGER = LoggerFactory.getLogger(Db4oCurrencyRateProvider.class);

	public Db4oCurrencyRateProvider(String dbFileName) {
		this(dbFileName, DataVersion.CURRENT_VERSION);
	}

	public Db4oCurrencyRateProvider(String dbFileName, int dataVersion) {
		super();
		this.dbFileName = dbFileName;
		this.dataVersion = dataVersion;
	}

	@Override public synchronized void init() {
		File dbFile = new File(this.dbFileName);
		dbFile.getParentFile().mkdirs();
		boolean existed = dbFile.exists();
		openDb();
		DataVersion newVersion = new DataVersion(this.dataVersion);
		if (existed) {
			DataVersion oldVersion = exactlyOne(db.query(DataVersion.class), null);
			if (oldVersion == null || oldVersion.compareTo(newVersion) < 0)
				upgradeData(oldVersion, newVersion);
		}
		else
			setDataVersion(newVersion);
	}

	private void openDb() {
		EmbeddedConfiguration dbConfig = Db4oEmbedded.newConfiguration();
		ObjectClass currencyRateConfig = dbConfig.common().objectClass(CurrencyRatesObject.class);
		currencyRateConfig.objectField("baseCurrency").indexed(true);
		currencyRateConfig.objectField("currency").indexed(true);
		currencyRateConfig.cascadeOnUpdate(true);
		currencyRateConfig.cascadeOnDelete(true);
		db = Db4oEmbedded.openFile(dbConfig, dbFileName);
	}

	private void upgradeData(DataVersion oldVersion, DataVersion newVersion) {
		if (oldVersion != null)
			LOGGER.info("Upgrading cached data...");
		db.close();
		new File(dbFileName).delete();
		openDb();
		setDataVersion(newVersion);
		if (oldVersion != null)
			LOGGER.info("Cached data upgrade finished.");
	}

	private void setDataVersion(final DataVersion newVersion) {
		doInDb4o(db -> db.store(newVersion));
	}

	@Override public synchronized void close() {
		if (db != null)
			db.close();
		closed = true;
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		return queryDb4o(db -> {
			CurrencyRatesObject rate = getCurrencyRate(db, baseCurrency, currency);
			return rate != null ? rate.getRate(date) : null;
		});
	}

	@Override public Map<Date, RateValue> getRates(String baseCurrency, String currency, Collection<Date> dates) {
		Map<Date, RateValue> rates = queryDb4o(db -> {
			Map<Date, RateValue> dateRates = new TreeMap<>();
			CurrencyRatesObject rate = getCurrencyRate(db, baseCurrency, currency);
			if (rate != null) {
				dateRates.putAll(rate.getRates());
				dateRates.keySet().retainAll(dates);
			}
			return dateRates;
		});
		return rates != null ? rates : Collections.<Date, RateValue>emptyMap();
	}

	@Override public void setRate(String baseCurrency, String currency, Date date, RateValue rateValue) {
		doInDb4o(db -> {
			CurrencyRatesObject rate = getCurrencyRate(db, baseCurrency, currency);
			if (rate == null)
				rate = new CurrencyRatesObject(baseCurrency, currency);
			rate.setRate(date, rateValue);
			db.store(rate);
		});
	}

	@Override public void setRates(String baseCurrency, String currency, Map<Date, RateValue> dateRates) {
		doInDb4o(db -> {
			CurrencyRatesObject rates = getCurrencyRate(db, baseCurrency, currency);
			if (rates == null)
				rates = new CurrencyRatesObject(baseCurrency, currency);
			rates.setRates(dateRates);
			db.store(rates);
		});
	}

	private CurrencyRatesObject getCurrencyRate(ObjectContainer db, String baseCurrency, String currency) {
		CurrencyRatesObject template = new CurrencyRatesObject(baseCurrency, currency);
		return exactlyOne(db.<CurrencyRatesObject>queryByExample(template), template);
	}

	private <T> T exactlyOne(ObjectSet<T> objectSet, T template) {
		switch (objectSet.size()) {
			case 0: return null;
			case 1: return objectSet.get(0);
			default: throw new IllegalStateException(String.format("Multiple objects found for template %1$s.", template));
		}
	}

	private synchronized void doInDb4o(Consumer<ObjectContainer> callback) {
		if (closed) return;
		try {
			callback.accept(db);
			db.commit();
		}
		catch (Throwable th) {
			db.rollback();
			throw th;
		}
	}

	private synchronized <T> T queryDb4o(Function<ObjectContainer, T> callback) {
		return !closed ? callback.apply(db) : null;
	}
}