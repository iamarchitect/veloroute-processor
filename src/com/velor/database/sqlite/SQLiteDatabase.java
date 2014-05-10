package com.velor.database.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.velor.storage.database.Cursor;
import com.velor.storage.database.CursorImpl;
import com.velor.storage.database.Database;

public final class SQLiteDatabase implements Database {

	public static final int CONFLICT_REPLACE = 0;

	private Connection connection;

	private boolean success = false;

	public boolean open(String file) {
		boolean result = false;
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + file);
			result = true;
		} catch (SQLException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void beginTransaction() {
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * End a transaction. See beginTransaction for notes about how to use this
	 * and when transactions are committed and rolled back.
	 */
	public void endTransaction() {
		try {
			if (!success) {
				connection.rollback();
			} else {
				connection.commit();
			}
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setTransactionSuccessful() {
		success = true;

	}

	public void beginTransactionNonExclusive() {
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean inTransaction() {
		try {
			return connection.getAutoCommit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isDbLockedByCurrentThread() {
		return false;
	}

	public Cursor query(boolean distinct, String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy, String limit) {

		return rawQuery(
				buildQuery(distinct, table, columns, selection, selectionArgs,
						groupBy, having, orderBy), selectionArgs);
	}

	protected String buildQuery(boolean distinct, String table,
			String[] columns, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		String sql = "SELECT ";

		if (columns != null) {
			sql += StringUtils.collectionToCommaDelimitedString(Arrays
					.asList(columns));
		} else {
			sql += " * ";
		}

		sql += " FROM " + table;

		if (selection != null) {
			sql += " WHERE " + selection;
		}

		if (groupBy != null) {
			sql += " GROUP BY " + groupBy;
		}

		if (having != null) {
			sql += " HAVING " + having;
		}

		if (orderBy != null) {
			sql += " ORDER BY " + orderBy;
		}

		return sql;
	}

	public Cursor query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		return query(false, table, columns, selection, selectionArgs, groupBy,
				having, orderBy, null /* limit */);
	}

	public Cursor query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {

		return query(false, table, columns, selection, selectionArgs, groupBy,
				having, orderBy, limit);
	}

	public Cursor rawQuery(String sql, String[] selectionArgs) {
		final PreparedStatement stmt;
		Cursor c = null;

		try {
			stmt = connection.prepareStatement(sql);

			int i = 1;
			if (selectionArgs != null) {
				for (String arg : selectionArgs) {
					stmt.setString(i++, arg);
				}
			}

			c = new CursorImpl(stmt.executeQuery()) {

				@Override
				public void close() {
					super.close();
					try {
						if (stmt != null) {
							stmt.close();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			};
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return c;
	}

	public long insertOrThrow(String table, String nullColumnHack /* ignored */,
			Map<String, Object> values) {
		return insert(table, null, values);
	}

	public long replaceOrThrow(String table, String nullColumnHack,
			Map<String, Object> initialValues) throws SQLException {
		return insertWithOnConflict(table, nullColumnHack, initialValues,
				CONFLICT_REPLACE);
	}

	public long insertWithOnConflict(String table,
			String nullColumnHack /* ignored */,
			Map<String, Object> initialValues, int conflictAlgorithm /* ignored */) {
		return insert(table, null, initialValues);
	}

	public int update(String table, Map<String, Object> values,
			String whereClause, String[] whereArgs) {

		String sql = "UPDATE " + table + " SET ";

		List<String> sets = new ArrayList<String>();
		for (String key : values.keySet()) {
			String value = key + "=";
			if (values.get(key) instanceof String) {
				value += "'" + values.get(key) + "'";
			} else {
				value += values.get(key);
			}
			sets.add(value);
		}
		sql += StringUtils.collectionToCommaDelimitedString(sets);
		sql += " WHERE " + whereClause;

		execSQL(sql, whereArgs);

		return 0;
	}

	public long insert(String table, Object object/* ignored */,
			Map<String, Object> row) {

		String sql = "INSERT OR REPLACE INTO " + table + "(";

		List<String> columns = new ArrayList<String>();

		for (String key : row.keySet()) {
			columns.add(key);
		}
		sql += StringUtils.collectionToCommaDelimitedString(columns)
				+ ") VALUES(";

		Character[] marks = new Character[columns.size()];
		Arrays.fill(marks, '?');
		sql += StringUtils.collectionToCommaDelimitedString(Arrays
				.asList(marks)) + ") ";

		long result = -1;
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			setParameters(row, ps);
			ps.executeUpdate();

			ResultSet rs = connection.createStatement().executeQuery(
					"SELECT last_insert_rowid()");
			if (rs.next()) {
				result = rs.getLong(1);
			}
			ps.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	protected void setParameters(Map<String, Object> row, PreparedStatement ps)
			throws SQLException {
		int i = 1;
		for (String key : row.keySet()) {
			Object value = row.get(key);
			if (value instanceof String) {
				ps.setString(i, value.toString());
			} else if (value instanceof byte[]) { // this is a blob
				ps.setBytes(i, (byte[]) value);
			} else if (value instanceof Integer) {
				ps.setInt(i, (int) value);
			} else if (value instanceof Double) {
				ps.setDouble(i, (double) value);
			} else if (value instanceof Float) {
				ps.setFloat(i, (float) value);
			} else if (value instanceof Long) {
				ps.setLong(i, (long) value);
			} else if (value instanceof Byte) {
				ps.setByte(i, (byte) value);
			}
			i++;
		}
	}

	protected void setParameters(String args[], PreparedStatement ps)
			throws SQLException {
		int i = 1;
		for (String value : args) {
			ps.setString(i, value);
			i++;
		}
	}

	public void execSQL(String sql) {
		// sql = sql.toLowerCase().replace("\n", "").replace("\t", "").trim()
		// .replaceAll("\\s+{2,}", " ");
		try {
			Statement s = connection.createStatement();
			s.executeUpdate(sql);
			s.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void execSQL(String sql, String[] args) {
		// sql = sql.toLowerCase().replace("\n", "").replace("\t", "").trim()
		// .replaceAll("\\s+{2,}", " ");
		// System.out.println(sql);
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			setParameters(args, ps);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void delete(String table, String where, String[] args) {
		String sql = "DELETE FROM " + table + " WHERE " + where;
		execSQL(sql, args);
	}

}
