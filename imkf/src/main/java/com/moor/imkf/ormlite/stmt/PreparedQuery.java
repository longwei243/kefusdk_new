package com.moor.imkf.ormlite.stmt;

import com.moor.imkf.ormlite.dao.Dao;

/**
 * Interface returned by the {@link QueryBuilder#prepare()} which supports custom SELECT queries. This should be in turn
 * passed to the {@link Dao#query(PreparedQuery)} or {@link Dao#iterator(PreparedQuery)} methods.
 * 
 * @param <T>
 *            The class that the code will be operating on.
 * @author graywatson
 */
public interface PreparedQuery<T> extends PreparedStmt<T> {
}
