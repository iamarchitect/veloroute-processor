package com.velor;

import java.util.Map;

import android.content.ContentValues;

public class DaoProcessor implements Preprocessor {
	private SqlDumperPropertyPlaceholderHelper sqlDumper;
	private DatabaseManager databaseManager;
	private String systemTable;

	public void setSqlDumper(SqlDumperPropertyPlaceholderHelper sqlDumper) {
		this.sqlDumper = sqlDumper;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public void setSystemTable(String systemTable) {
		this.systemTable = systemTable;
	}

	@Override
	public void preprocess() {
		Map<String, String> propertiesMap = sqlDumper.getPropertiesMap();
		databaseManager.execSQL("DELETE FROM " + systemTable + ";");
		for (String sql : propertiesMap.keySet()) {
			ContentValues row = new ContentValues();
			row.put("name", sql);
			row.put("value", propertiesMap.get(sql));
			databaseManager.createOrReplace(systemTable, row);
		}
	}

}
