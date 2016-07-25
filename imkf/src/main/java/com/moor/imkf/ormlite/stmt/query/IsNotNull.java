package com.moor.imkf.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.moor.imkf.ormlite.db.DatabaseType;
import com.moor.imkf.ormlite.field.FieldType;
import com.moor.imkf.ormlite.stmt.ArgumentHolder;
import com.moor.imkf.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'IS NOT NULL' comparison query part. Used by {@link Where#isNull}.
 * 
 * @author graywatson
 */
public class IsNotNull extends BaseComparison {

	public IsNotNull(String columnName, FieldType fieldType) throws SQLException {
		super(columnName, fieldType, null, true);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("IS NOT NULL ");
	}

	@Override
	public void appendValue(DatabaseType databaseType, StringBuilder sb, List<ArgumentHolder> argList) {
		// there is no value
	}
}
