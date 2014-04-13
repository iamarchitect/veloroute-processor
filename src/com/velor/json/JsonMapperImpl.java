package com.velor.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.ContentValues;
import android.os.Build.Parcel;

public class JsonMapperImpl implements JsonMapper {

	@SuppressWarnings("unchecked")
	public ContentValues map(Map<String, Object> object) {
		ContentValues result = null;
		Map<String, Object> map;
		Parcel parcel = Parcel.obtain();

		try {
			map = new HashMap<String, Object>();
			for (Iterator<String> it = object.keySet().iterator(); it.hasNext();) {
				String srcKey = it.next();
				String dstKey = getKey(srcKey);
				Object value = getValue(srcKey, object);
				if (value != null && dstKey != null) {
					puValue(map, dstKey, value);
				}
			}
			try {
				parcel.writeMap(map);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			parcel.setDataPosition(0);
			result = ContentValues.CREATOR.createFromParcel(parcel);
			parcel.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	protected String getKey(String key) {
		return key;
	}

	protected Object getValue(String key, Map<String, Object> object)
			throws Exception {

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
	public ContentValues map(Map<String, Object> c, ContentValues dest) {
		dest.clear();
		dest.putAll(map(c));
		return dest;
	}

}
