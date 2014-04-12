package app.velor.json;

import java.io.File;
import java.util.Map;

public class JsonParserHandler implements JsonParser {
	private Map<String, JsonParser> parsers;

	public void setParsers(Map<String, JsonParser> parsers) {
		this.parsers = parsers;
	}

	@Override
	public boolean parseJSON(File f) throws Exception {
		// int pos = f != null ? f.getName().lastIndexOf('.') : -1;

		String[] tokens = f != null ? f.getName().split("\\.")
				: new String[] {};

		// if (pos > 0) {
		// String filename = f.getName().substring(0, pos);

		if (tokens.length >= 2 && "json".equals(tokens[tokens.length - 1])) {
			String filename = tokens[tokens.length - 2];
			JsonParser parser = parsers.get(filename);
			if (parser != null) {
				return parser.parseJSON(f);
			}
		}
		// }
		return false;
	}
}
