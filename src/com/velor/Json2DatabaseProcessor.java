package com.velor;

import java.io.File;
import java.util.Arrays;

import com.velor.json.JsonParser;

public class Json2DatabaseProcessor implements Preprocessor {
	private String destinationFolder;
	private String databaseName;
	private JsonParser parser;

	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public void setParser(JsonParser parser) {
		this.parser = parser;
	}

	@Override
	public void preprocess() {
		try {
			importJsons(destinationFolder, databaseName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void importJsons(String destinationFolder, String databaseName)
			throws Exception {

		File[] params = new File(destinationFolder).listFiles();

		params = Arrays.copyOf(params, params.length + 1);
		// params[params.length - 1] = new File(new URI(
		// "file:///android_asset/slsj-streets.json"));

		for (File file : params) {
			if (parser.parseJSON(file)) {
				System.out.println("inserted succesfully " + file);
			} else {
				System.out.println("skipped " + file);
			}
			System.gc();
		}

	}
}
