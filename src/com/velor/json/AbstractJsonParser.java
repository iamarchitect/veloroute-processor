package com.velor.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public abstract class AbstractJsonParser implements JsonParser {

	protected Gson gson;

	public boolean parseJSON(File f) throws Exception {
		boolean result = false;
		gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader(f));
		try {
			prepareParsing();
			prepareReader(reader);

			while (reader.hasNext()) {
				handleNextValue(reader);
			}
			closeReader(reader);
			markAsSuccess();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			markAsFailure();
			throw e;
		}

		return result;
	}

	protected abstract void closeReader(JsonReader reader) throws IOException;

	protected abstract void prepareReader(JsonReader reader) throws IOException;

	protected abstract void prepareParsing();

	protected abstract void markAsSuccess();

	protected abstract void markAsFailure();

	protected abstract void handleNextValue(JsonReader reader);

}
