package org.strangeforest.currencywatch.jdbc;

import java.sql.*;
import java.util.Date;
import java.util.*;
import javax.sql.*;

import org.strangeforest.currencywatch.core.*;

import com.finsoft.db.*;
import com.finsoft.db.gateway.*;

import static com.finsoft.db.gateway.DataHelper.*;

public class JDBCCurrencyRateProvider extends BaseCurrencyRateProvider implements UpdatableCurrencyRateProvider {

	private final SchemaManager schemaManager;
	private final DBGateway db;

	public JDBCCurrencyRateProvider(SchemaManager schemaManager, DataSource dataSource, String dialect) {
		super();
		this.schemaManager = schemaManager;
		db = new DBGateway(dataSource, SQLsFactory.getSQLs(getClass(), dialect));
	}

	@Override public void init() {
		schemaManager.ensureSchema();
	}

	@Override public RateValue getRate(final String baseCurrency, final String currency, final Date date) {
		return db.fetchOne("FetchRate", RATE_VALUE_READER, new StatementPreparer() {
			@Override public void prepare(PreparedStatementHelper st) throws SQLException {
				setString(st, "baseCurrency", baseCurrency);
				setString(st, "currency", currency);
				setDate(st, "date", date);
			}
		});
	}

	@Override public void setRate(final String baseCurrency, final String currency, final Date date, final RateValue rateValue) {
		db.executeUpdate("MergeRate", new StatementPreparer() {
			@Override public void prepare(PreparedStatementHelper st) throws SQLException {
				setString(st, "baseCurrency", baseCurrency);
				setString(st, "currency", currency);
				setDate(st, "date", date);
				setRateValue(st, rateValue);
			}
		});
	}

	@Override public void setRates(final String baseCurrency, final String currency, final Map<Date, RateValue> dateRates) {
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

	private static final ObjectReader<RateValue> RATE_VALUE_READER = new ObjectReader<RateValue>() {
		@Override public RateValue read(ResultSet rs) throws SQLException {
			return new RateValue(
				getBigDecimal(rs, "BID"),
				getBigDecimal(rs, "ASK"),
				getBigDecimal(rs, "MIDDLE")
			);
		}
	};

	private static void setRateValue(PreparedStatementHelper st, RateValue rateValue) throws SQLException {
		setDecimal(st, "bid", rateValue.getBid());
		setDecimal(st, "ask", rateValue.getAsk());
		setDecimal(st, "middle", rateValue.getMiddle());
	}
}