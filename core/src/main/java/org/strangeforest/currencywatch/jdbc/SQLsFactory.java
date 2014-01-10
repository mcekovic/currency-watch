package org.strangeforest.currencywatch.jdbc;

import java.io.*;

import org.strangeforest.db.gateway.*;

public abstract class SQLsFactory {

	public static SQLs getSQLs(Class<?> cls, String dialect) {
		String sqlsName = cls.getSimpleName();
		if (dialect != null)
			sqlsName += '.' + dialect;
		sqlsName += ".sqls";
		InputStream in = cls.getResourceAsStream(sqlsName);
		if (in != null)
			return new SQLs(in);
		else
			throw new DBException("Cannot find SQLs: " + sqlsName);
	}
}
