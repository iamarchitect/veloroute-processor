package app.velor.json.impl;

import java.util.Map;

public class PoiTypeMapperHelper extends BaseJsonMapperHelper {
	private static final String JSON_ICON = "icon";
	private String mediaUrl;

	public PoiTypeMapperHelper(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	@Override
	public Object getValue(String key, Map<String, Object> object) {
		Object value = object.get(key);
		value = getValueOrBytes(mediaUrl, JSON_ICON, key, value);
		return value;
	}

	@Override
	public boolean puValue(Map<String, Object> map, String key, Object value) {
		return false;
	}

}
