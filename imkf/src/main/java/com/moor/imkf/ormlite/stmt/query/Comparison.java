package com.moor.imkf.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.moor.imkf.ormlite.db.DatabaseType;
import com.moor.imkf.ormlite.stmt.ArgumentHolder;

/**
 * Internal interfaces which define a comparison operation.
 * 
 * @author graywatson
 */
interface Comparison extends Clause {

	/**
	 * Return the column-name associated with the comparison.
	 */
	public String getColumnName();

	/**
	 * Add the operation used in this comparison to the string builder.
	 */
	public void appendOperation(StringBuilder sb);

	/**
	 * Add the value of the comparison to the string builder.
	 */
	public void appendValue(DatabaseType databaseType, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException;
}
