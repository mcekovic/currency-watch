package org.strangeforest.currencywatch.jdbc;

import java.sql.*;

import org.slf4j.*;

import com.finsoft.db.*;

public class SLF4JConnectionPoolLogger implements ConnectionPoolLogger {

	private final Logger logger;

	public SLF4JConnectionPoolLogger(String loggerName) {
		super();
		logger = LoggerFactory.getLogger(loggerName);
	}

	@Override public void logMessage(String message) {
		logger.debug(message);
	}

	@Override public void logError(String message, Throwable th) {
		logger.warn(message, th);
	}

	@Override public void logStatement(Statement st) {
		if (logger.isTraceEnabled())
			logger.trace(String.valueOf(st));
	}

	@Override public void logPreparedStatement(PreparedStatement pst) {
		if (logger.isTraceEnabled())
			logger.trace(String.valueOf(pst));
	}

	@Override public void logCallableStatement(CallableStatement call) {
		if (logger.isTraceEnabled())
			logger.trace(String.valueOf(call));
	}
}