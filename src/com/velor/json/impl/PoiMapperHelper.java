package com.velor.json.impl;

import java.util.Map;

public class PoiMapperHelper extends BaseJsonMapperHelper {
	private static final String JSON_PICTURE = "photo";
	private static final String JSON_POSITION = "position";

	private String mediaUrl;

	public PoiMapperHelper(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	@Override
	public boolean puValue(Map<String, Object> map, String key, Object value) {
		boolean result = false;
		// parse the Django POINT object
		if (JSON_POSITION.equals(key)) {
			String[] tokens = ((String) value).replace("POINT (", "")
					.replace(")", "").split(" ");

			map.put("latitude", Double.parseDouble(tokens[1]));
			map.put("longitude", Double.parseDouble(tokens[0]));
			result = true;
		}

		return result;
	}

	@Override
	public Object getValue(String key, Map<String, Object> object) {
		Object value = object.get(key);
		value = getValueOrBytes(mediaUrl, JSON_PICTURE, key, value);
		return value;
	}

}
