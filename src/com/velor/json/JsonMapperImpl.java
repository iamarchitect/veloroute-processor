package com.velor.json;

import java.util.HashMap;
import java.util.Map;

public class JsonMapperImpl implements JsonMapper {

	public Map<String, Object> map(Map<String, Object> object) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (String srcKey : object.keySet()) {
			String dstKey = getKey(srcKey);
			Object value = getValue(srcKey, object);
			if (value != null && dstKey != null) {
				puValue(result, dstKey, value);
			}
		}
		return result;
	}

	protected String getKey(String key) {
		return key;
	}

	protected Object getValue(String key, Map<String, Object> object) {

		Object result = null;
		if (object.get(key) != null) {
			result = object.get(key);
		}

		return result;
	}

	protected void puValue(Map<String, Object> map, String key, Object value) {
		if (!"?IGNORED".equals(key) && key != null && value != null) {
			map.put(key, value);
		}
	}

	@Override
	public Map<String, Object> map(Map<String, Object> c,
			Map<String, Object> dest) {
		dest.clear();
		dest.putAll(map(c));
		return dest;
	}

}
