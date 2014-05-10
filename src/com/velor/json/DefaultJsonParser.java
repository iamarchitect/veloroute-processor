package com.velor.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.velor.DatabaseManager;

public class DefaultJsonParser extends AbstractJsonParser {
	protected DatabaseManager databaseManager;
	private JsonMapper mapper;
	private String table;

	public DefaultJsonParser(DatabaseManager databaseManager,
			JsonMapper mapper, String table) {
		this.databaseManager = databaseManager;
		this.mapper = mapper;
		this.table = table;
	}

	protected void closeReader(JsonReader reader) throws IOException {
		reader.endArray();
		reader.skipValue();
		reader.skipValue();
		reader.endObject();
		reader.close();
	}

	protected void prepareReader(JsonReader reader) throws IOException {
		reader.beginObject();
		reader.nextName();
		reader.beginArray();
	}

	protected void prepareParsing() {
	}

	protected void markAsSuccess() {
	}

	protected void markAsFailure() {
	}

	protected void handleNextValue(JsonReader reader) {
		Type stringStringMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		Map<String, Object> map = gson.fromJson(reader, stringStringMap);
		handleNextValue(map);
	}

	protected void handleNextValue(Map<String, Object> map) {
		Map<String, Object> values = mapper.map(map);
		databaseManager.create(table, values);
	}

}
