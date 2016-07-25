package com.moor.imkf.ormlite.field.types;

import com.moor.imkf.ormlite.field.SqlType;

/**
 * Type that persists a long primitive.
 * 
 * @author graywatson
 */
public class LongType extends LongObjectType {

	private static final LongType singleTon = new LongType();

	public static LongType getSingleton() {
		return singleTon;
	}

	private LongType() {
		super(SqlType.LONG, new Class<?>[] { long.class });
	}

	/**
	 * Here for others to subclass.
	 */
	protected LongType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}
}
