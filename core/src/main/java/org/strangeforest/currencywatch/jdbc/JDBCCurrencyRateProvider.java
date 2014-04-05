package org.strangeforest.currencywatch.jdbc;

import java.sql.*;
import java.util.Date;
import java.util.*;
import javax.sql.*;

import org.strangeforest.currencywatch.core.*;
import org.strangeforest.db.*;
import org.strangeforest.db.gateway.*;

import static org.strangeforest.db.gateway.DataHelper.*;

public class JDBCCurrencyRateProvider implements UpdatableCurrencyRateProvider {

	private final SchemaManager schemaManager;
	private final DBGateway db;

	public JDBCCurrencyRateProvider(SchemaManager schemaManager, DataSource dataSource) {
		super();
		this.schemaManager = schemaManager;
		db = new DBGateway(dataSource, SQLsFactory.getSQLs(getClass(), schemaManager.getDialect()));
	}

	@Override public void init() {
		schemaManager.ensureSchema();
	}

	@Override public RateValue getRate(String baseCurrency, String currency, Date date) {
		return db.fetchOne("FetchRate", JDBCCurrencyRateProvider::readRateValue, (PreparedStatementHelper st) -> {
			setString(st, "baseCurrency", baseCurrency);
			setString(st, "currency", currency);
			setDate(st, "date", date);
		});
	}

	@Override public void setRate(String baseCurrency, String currency, Date date, RateValue rateValue) {
		db.executeUpdate("MergeRate", (PreparedStatementHelper st) -> {
			setString(st, "baseCurrency", baseCurrency);
			setString(st, "currency", currency);
			setDate(st, "date", date);
			setRateValue(st, rateValue);
		});
	}

	@Override public void setRates(String baseCurrency, String currency, Map<Date, RateValue> dateRates) {
		db.executeBatchUpdate("MergeRate", new BatchStatementPreparer() {
			private final Iterator<Map.Entry<Date, RateValue>> iter = dateRates.entrySet().iterator();
			@Override public void prepareOnce(PreparedStatementHelper st) throws SQLException {
				setString(st, "baseCurrency", baseCurrency);
				setString(st, "currency", currency);
			}
			@Override public boolean hasMore() {
				return iter.hasNext();
			}
			@Override public void prepare(PreparedStatementHelper st) throws SQLException {
				Map.Entry<Date, RateValue> dateRate = iter.next();
				setDate(st, "date", dateRate.getKey());
				setRateValue(st, dateRate.getValue());
			}
		});
	}

	private static RateValue readRateValue(ResultSet rs) throws SQLException {
		return new RateValue(
			getBigDecimal(rs, "BID"),
			getBigDecimal(rs, "ASK"),
			getBigDecimal(rs, "MIDDLE")
		);
	}

	private static void setRateValue(PreparedStatementHelper st, RateValue rateValue) throws SQLException {
		setDecimal(st, "bid", rateValue.getBid());
		setDecimal(st, "ask", rateValue.getAsk());
		setDecimal(st, "middle", rateValue.getMiddle());
	}
}