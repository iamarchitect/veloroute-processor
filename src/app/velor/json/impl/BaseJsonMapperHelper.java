package app.velor.json.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import app.velor.json.JsonMapperFactory;
import app.velor.json.JsonMapperFactory.JsonMapperHelper;

public abstract class BaseJsonMapperHelper implements JsonMapperHelper {

	protected BaseJsonMapperHelper() {
		super();
	}

	protected byte[] getBytes(String urlStr) {
		byte data[] = new byte[1024];
		HttpURLConnection connexion = null;
		URL url = null;
		InputStream is = null;
		try {
			url = new URL(urlStr);
			connexion = (HttpURLConnection) url.openConnection();
			connexion.setRequestMethod("GET");
			connexion.connect();
			is = connexion.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);

			int count = 0;
			// download the data
			while ((count = is.read(data)) != -1) {
				bos.write(data);
			}
			data = bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (connexion != null) {
				connexion.disconnect();
			}
		}

		return data;
	}

	/**
	 * 
	 * @param url
	 * @param keyForBytes
	 * @param key
	 * @param value
	 * @return
	 * @throws JSONException
	 */
	protected Object getValueOrBytes(String url, String keyForBytes,
			String key, Object value) {

		Object result = null;
		// Retrieve the image from its URL
		if (keyForBytes.equals(key) && value != null) {

			// don't insert an empty string
			if (value.equals("")) {
				value = null;
			}

			else {
				String name = (String) value;
				url += name;
				value = getBytes(url);
			}

			result = value;
		}

		return result;
	}
}
