package com.moor.imkf.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.moor.imkf.ormlite.db.DatabaseType;
import com.moor.imkf.ormlite.stmt.ArgumentHolder;
import com.moor.imkf.ormlite.stmt.QueryBuilder.InternalQueryBuilderWrapper;
import com.moor.imkf.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'EXISTS' query part. Used by {@link Where#exists}.
 * 
 * @author graywatson
 */
public class Exists implements Clause {

	private final InternalQueryBuilderWrapper subQueryBuilder;

	public Exists(InternalQueryBuilderWrapper subQueryBuilder) {
		this.subQueryBuilder = subQueryBuilder;
	}

	public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException {
		sb.append("EXISTS (");
		subQueryBuilder.appendStatementString(sb, argList);
		sb.append(") ");
	}
}
