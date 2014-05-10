package com.velor.json.impl;

import java.util.Map;

import com.velor.json.JsonMapperFactory.JsonMapperHelper;

public class RouteSectionsMapperHelper implements JsonMapperHelper {

	@Override
	public Object getValue(String key, Map<String, Object> object) {
		if ("color".equals(key)) {
			String color = (String) object.get(key);
			color = color.replace("#", "");
			return "".equals(color) || color == null ? 0xFF000000 : Integer
					.parseInt(color, 16) | 0xFF000000;
		}
		return null;
	}

	@Override
	public boolean puValue(Map<String, Object> map, String key, Object value) {
		return false;
	}
}
