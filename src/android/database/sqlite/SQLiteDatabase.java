package android.database.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import android.content.ContentValues;

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
		PreparedStatement stmt = null;
		Cursor c = null;

		try {
			stmt = connection.prepareStatement(sql);

			int i = 1;
			if (selectionArgs != null) {
				for (String arg : selectionArgs) {
					stmt.setString(i++, arg);
				}
			}

			c = new CursorImpl(stmt.executeQuery());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return c;
	}

	public long insertOrThrow(String table, String nullColumnHack /* ignored */,
			ContentValues values) {
		return insert(table, null, values);
	}

	public long replaceOrThrow(String table, String nullColumnHack,
			ContentValues initialValues) throws SQLException {
		return insertWithOnConflict(table, nullColumnHack, initialValues,
				CONFLICT_REPLACE);
	}

	public long insertWithOnConflict(String table,
			String nullColumnHack /* ignored */, ContentValues initialValues,
			int conflictAlgorithm /* ignored */) {
		return insert(table, null, initialValues);
	}

	public int update(String table, ContentValues values, String whereClause,
			String[] whereArgs) {

		String sql = "UPDATE " + table + " SET ";

		List<String> sets = new ArrayList<String>();
		for (Entry<String, Object> vs : values.valueSet()) {
			String set = vs.getKey() + " = ";
			if (vs.getValue() instanceof String) {
				set += "'" + vs.getValue() + "'";
			} else {
				set += vs.getValue();
			}
			sets.add(set);
		}
		sql += StringUtils.collectionToCommaDelimitedString(sets);
		sql += " WHERE " + whereClause;

		execSQL(sql, whereArgs);

		return 0;
	}

	public long insert(String table, Object object/* ignored */, ContentValues row) {

		String sql = "INSERT OR REPLACE INTO " + table + "(";

		List<String> columns = new ArrayList<String>();

		for (Entry<String, Object> vs : row.valueSet()) {
			columns.add(vs.getKey());
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
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	protected void setParameters(ContentValues row, PreparedStatement ps)
			throws SQLException {
		int i = 1;
		for (Entry<String, Object> vs : row.valueSet()) {
			Object value = vs.getValue();
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
			connection.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void execSQL(String sql, String[] args) {
		// sql = sql.toLowerCase().replace("\n", "").replace("\t", "").trim()
		// .replaceAll("\\s+{2,}", " ");
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			setParameters(args, ps);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void delete(String table, String where, String[] args) {
		String sql = "DELETE FROM " + table + " WHERE " + where;
		execSQL(sql, args);
	}

}
