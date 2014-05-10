package com.velor;

import java.util.Map;

import com.velor.database.sqlite.SQLiteDatabase;
import com.velor.storage.database.Cursor;

public class DatabaseManager {
	protected String[] createSql;
	protected String[] dropTableSql;
	protected String[] dropViewSql;

	protected SQLiteDatabase database;

	public void setCreateSql(String[] createSql) {
		this.createSql = createSql;
	}

	public void setDropTableSql(String[] dropTableSql) {
		this.dropTableSql = dropTableSql;
	}

	public void setDropViewSql(String[] dropViewSql) {
		this.dropViewSql = dropViewSql;
	}

	public SQLiteDatabase getDatabase() {
		return database;
	}

	public void setDatabase(SQLiteDatabase database) {
		this.database = database;
	}

	public void open(String name) {
		database.open(name);
	}

	public void close() {
		database.close();
	}

	public void createTables() {
		for (String sql : createSql) {
			String[] allQueries = sql.split(";");
			int n = allQueries.length;
			for (int i = 0; i < n; i++) {
				String qry = allQueries[i].replace("\n", "").replace("\t", "");
				if (!"".equals(qry)) {
					database.execSQL(allQueries[i] + ";");
				}
			}
		}
	}

	public void dropTables() {
		for (String sql : dropTableSql) {
			database.execSQL(sql);
		}
	}

	public long[] create(String table, Map<String, Object>... rows) {
		if (rows.length > 1) {
			beginTransaction();
		}

		long[] result = new long[rows.length];

		int i = 0;
		for (Map<String, Object> row : rows) {
			result[i] = database.insertOrThrow(table,
					null /* nullColumnHack */, row);
			i++;
		}

		if (rows.length > 1) {
			endTransactionSuccessful();
		}

		return result;
	}

	public int update(String table, Map<String, Object> row, String where,
			String... args) {
		return database.update(table, row, where, args);
	}

	public long[] createOrReplace(String table, Map<String, Object>... rows) {
		if (rows.length > 1) {
			beginTransaction();
		}

		long[] result = new long[rows.length];
		int i = 0;

		for (Map<String, Object> row : rows) {
			result[i] = database.insertWithOnConflict(table,
					null /* nullColumnHack */, row,
					SQLiteDatabase.CONFLICT_REPLACE);
			i++;
		}

		if (rows.length > 1) {
			endTransactionSuccessful();
		}
		return result;
	}

	public void beginTransaction() {
		database.beginTransaction();
	}

	public void endTransactionSuccessful() {
		database.setTransactionSuccessful();
		database.endTransaction();
	}

	public void rollback() {
		if (database.inTransaction()) {
			database.endTransaction();
		}
	}

	public boolean execSQL(String sql) {
		database.execSQL(sql);
		return true;
	}

	public void execSQL(String sql, String[] args) {
		database.execSQL(sql, args);
	}

	public Cursor rawQuery(String sql, String[] args) {
		return database.rawQuery(sql, args);
	}

	public Cursor query(boolean distinct, String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy, String limit) {
		return database.query(distinct, table, columns, selection,
				selectionArgs, groupBy, having, orderBy, limit);
	}

	public Cursor query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		return database.query(table, columns, selection, selectionArgs,
				groupBy, having, orderBy);
	}

	public Cursor query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		return database.query(table, columns, selection, selectionArgs,
				groupBy, having, orderBy, limit);
	}

	public void delete(String table, String where, String[] args) {
		database.delete(table, where, args);
	}

}
