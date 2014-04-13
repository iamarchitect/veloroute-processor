package com.velor.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.velor.json.JsonMapperFactory.JsonMapperHelper;
import com.velor.storage.mapper.Mapper;
import com.velor.storage.mapper.MapperFactory;

import android.content.ContentValues;

public class JsonMapperFactory
		extends
		MapperFactory<ContentValues, Map<String, Object>, String, JsonMapperHelper> {

	public interface JsonMapperHelper {
		Object getValue(String key, Map<String, Object> object)
				throws Exception;

		boolean puValue(Map<String, Object> map, String key, Object value);
	}

	public JsonMapperFactory() {
		super();
	}

	private Map<String, String> getMappings(String properties) {
		Map<String, String> result = new HashMap<String, String>();
		try {
			Properties props = new Properties();
			InputStream in = getClass().getResourceAsStream(properties);
			props.load(in);
			for (Object key : props.keySet()) {
				result.put((String) key, (String) props.get(key));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	public JsonMapper getMapper() {
		JsonMapperImpl mapper = new JsonMapperImpl();
		return mapper;
	}

	@Override
	protected Mapper<ContentValues, Map<String, Object>> createMapper(
			String mappings) {
		final Map<String, String> map = getMappings(mappings);
		JsonMapperImpl mapper = new JsonMapperImpl() {
			@Override
			protected String getKey(String key) {
				return map.containsKey(key) ? map.get(key) : key;
			}
		};

		return mapper;
	}

	@Override
	protected Mapper<ContentValues, Map<String, Object>> createMapper(
			String mappings, final JsonMapperHelper helper) {
		final Map<String, String> map = mappings != null ? getMappings(mappings)
				: new HashMap<String, String>();

		JsonMapperImpl mapper = new JsonMapperImpl() {
			@Override
			protected String getKey(String key) {
				return map.containsKey(key) ? map.get(key) : key;
			}

			@Override
			protected Object getValue(String key, Map<String, Object> object)
					throws Exception {
				Object result = helper.getValue(key, object);
				return result != null ? result : super.getValue(key, object);
			}

			@Override
			protected void puValue(Map<String, Object> map, String key,
					Object value) {
				boolean result = helper.puValue(map, key, value);
				if (!result) {
					super.puValue(map, key, value);
				}
			}

		};

		return mapper;
	}

}
