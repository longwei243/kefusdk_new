package com.moor.imkf.ormlite.field;

import java.sql.SQLException;

import com.moor.imkf.ormlite.support.DatabaseResults;

/**
 * Base class for field-converters.
 * 
 * @author graywatson
 */
public abstract class BaseFieldConverter implements FieldConverter {

	/**
	 * @throws SQLException
	 *             If there are problems with the conversion.
	 */
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
		// noop pass-thru
		return javaObject;
	}

	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		Object value = resultToSqlArg(fieldType, results, columnPos);
		if (value == null) {
			return null;
		} else {
			return sqlArgToJava(fieldType, value, columnPos);
		}
	}

	/**
	 * @throws SQLException
	 *             If there are problems with the conversion.
	 */
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
		// noop pass-thru
		return sqlArg;
	}

	public boolean isStreamType() {
		return false;
	}
}
