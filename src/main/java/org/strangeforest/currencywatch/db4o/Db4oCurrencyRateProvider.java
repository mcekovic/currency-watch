package org.strangeforest.currencywatch.db4o;

import java.io.*;
import java.util.*;

import org.strangeforest.currencywatch.core.*;

import com.db4o.*;
import com.db4o.config.*;

public class Db4oCurrencyRateProvider extends BaseCurrencyRateProvider implements UpdatableCurrencyRateProvider {

	private final String dbFileName;
	private ObjectContainer db;
	private boolean closed;

	private static final DataVersion CURRENT_VERSION = new DataVersion(1);

	public Db4oCurrencyRateProvider(String dbFileName) {
		super();
		this.dbFileName = dbFileName;
		File dbFile = new File(dbFileName);
		dbFile.getParentFile().mkdirs();
		boolean existed = dbFile.exists();
		openDb();
		if (existed) {
			DataVersion version = exactlyOne(db.query(DataVersion.class), null);
			if (version == null || version.compareTo(CURRENT_VERSION) < 0)
				upgradeData(version);
		}
	}

	private void openDb() {
		EmbeddedConfiguration dbConfig = Db4oEmbedded.newConfiguration();
		ObjectClass currencyRateConfig = dbConfig.common().objectClass(CurrencyRateObject.class);
		currencyRateConfig.objectField("symbolFrom").indexed(true);
		currencyRateConfig.objectField("symbolTo").indexed(true);
		currencyRateConfig.cascadeOnUpdate(true);
		currencyRateConfig.cascadeOnDelete(true);
		db = Db4oEmbedded.openFile(dbConfig, dbFileName);
	}

	private void upgradeData(DataVersion version) {
		if (version != null)
			System.out.println("Upgrading cached data...");
		db.close();
		new File(dbFileName).delete();
		openDb();
		doInDb4o(new Db4oCallback() {
			@Override public void doInDb4o(ObjectContainer db) {
				db.store(CURRENT_VERSION);
			}
		});
		if (version != null)
			System.out.println("Cached data upgrade finished.");
	}

	@Override public synchronized void close() {
		db.close();
		closed = true;
		super.close();
	}

	@Override public RateValue getRate(final String symbolFrom, final String symbolTo, final Date date) {
		return queryDb4o(new Db4oQueryCallback<RateValue>() {
			@Override public RateValue queryDb4o(ObjectContainer db) {
				CurrencyRateObject rate = getCurrencyRate(db, symbolFrom, symbolTo);
				return rate != null ? rate.getRate(date) : null;
			}
		});
	}

	@Override public Map<Date, RateValue> getRates(final String symbolFrom, final String symbolTo, final Collection<Date> dates) {
		Map<Date, RateValue> rates = queryDb4o(new Db4oQueryCallback<Map<Date, RateValue>>() {
			@Override public Map<Date, RateValue> queryDb4o(ObjectContainer db) {
				Map<Date, RateValue> dateRates = new HashMap<>();
				CurrencyRateObject rate = getCurrencyRate(db, symbolFrom, symbolTo);
				if (rate != null) {
					dateRates.putAll(rate.getRates());
					dateRates.keySet().retainAll(dates);
				}
				return dateRates;
			}
		});
		return rates != null ? rates : new HashMap<Date, RateValue>();
	}

	@Override public void setRate(final String symbolFrom, final String symbolTo, final Date date, final RateValue rateValue) {
		doInDb4o(new Db4oCallback() {
			@Override public void doInDb4o(ObjectContainer db) {
				CurrencyRateObject rate = getCurrencyRate(db, symbolFrom, symbolTo);
				if (rate == null)
					rate = new CurrencyRateObject(symbolFrom, symbolTo);
				rate.setRate(date, rateValue);
				db.store(rate);
			}
		});
	}

	@Override public void setRates(final String symbolFrom, final String symbolTo, final Map<Date, RateValue> dateRates) {
		doInDb4o(new Db4oCallback() {
			@Override public void doInDb4o(ObjectContainer db) {
				CurrencyRateObject rate = getCurrencyRate(db, symbolFrom, symbolTo);
				if (rate == null)
					rate = new CurrencyRateObject(symbolFrom, symbolTo);
				rate.setRates(dateRates);
				db.store(rate);
			}
		});
	}

	private CurrencyRateObject getCurrencyRate(ObjectContainer db, String symbolFrom, String symbolTo) {
		CurrencyRateObject template = new CurrencyRateObject(symbolFrom, symbolTo);
		return exactlyOne(db.<CurrencyRateObject>queryByExample(template), template);
	}

	private <T> T exactlyOne(ObjectSet<T> objectSet, T template) {
		switch (objectSet.size()) {
			case 0: return null;
			case 1: return objectSet.get(0);
			default: throw new IllegalStateException(String.format("Multiple objects found for template %1$s.", template));
		}
	}

	private synchronized void doInDb4o(Db4oCallback callback) {
		if (closed) return;
		try {
			callback.doInDb4o(db);
			db.commit();
		}
		catch (Throwable th) {
			db.rollback();
			throw th;
		}
	}

	private synchronized <T> T queryDb4o(Db4oQueryCallback<T> callback) {
		return !closed ? callback.queryDb4o(db) : null;
	}

	private static interface Db4oCallback {
		public void doInDb4o(ObjectContainer db);
	}

	private static interface Db4oQueryCallback<T> {
		public T queryDb4o(ObjectContainer db);
	}
}