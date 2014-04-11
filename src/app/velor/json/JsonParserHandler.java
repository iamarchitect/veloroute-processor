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
		int pos = f != null ? f.getName().lastIndexOf('.') : -1;

		if (pos > 0) {
			String filename = f.getName().substring(0, pos);

			JsonParser parser = parsers.get(filename);
			if (parser != null) {
				return parser.parseJSON(f);
			}
		}
		return false;
	}
}
