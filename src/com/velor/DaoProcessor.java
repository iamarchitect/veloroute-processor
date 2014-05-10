package com.velor;

import java.util.HashMap;
import java.util.Map;

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
		System.out.println("Preparing system tables");
		Map<String, String> propertiesMap = sqlDumper.getPropertiesMap();
		databaseManager.execSQL("DELETE FROM " + systemTable + ";");
		for (String sql : propertiesMap.keySet()) {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put("name", sql);
			row.put("value", propertiesMap.get(sql));
			databaseManager.createOrReplace(systemTable, row);
		}
	}

}
