package com.velor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class SqlDumperPropertyPlaceholderHelper extends
		PropertyPlaceholderConfigurer implements Preprocessor {

	private Map<String, String> propertiesMap;
	private String dmpsqlDir;

	public void setDmpsqlDir(String dmpsqlDir) {
		this.dmpsqlDir = dmpsqlDir;
	}

	public SqlDumperPropertyPlaceholderHelper() {
		super();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void processProperties(
			ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException {

		File user = new File("config.properties");
		InputStream in = null;
		if (user.exists()) {
			try {
				in = new FileInputStream(user);
				props.load(in);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ignored) {
					ignored.printStackTrace();
				}
			}
		}

		super.processProperties(beanFactory, props);

		propertiesMap = new HashMap<String, String>();
		for (Object key : props.keySet()) {
			String keyStr = key.toString();

			if (keyStr.toLowerCase().endsWith("sql")) {
				propertiesMap.put(
						keyStr,
						parseStringValue(props.getProperty(keyStr), props,
								new HashSet<Object>()));
			}
		}
	}

	public Map<String, String> getPropertiesMap() {
		return propertiesMap;
	}

	@Override
	public void preprocess() {

		File dst = new File(dmpsqlDir);
		if (!dst.exists()) {
			dst.mkdirs();
		}

		for (String statment : propertiesMap.keySet()) {
			File out = new File(dmpsqlDir + "/"
					+ statment.toLowerCase().replace("_sql", "") + ".sql");
			FileWriter fw = null;
			try {
				fw = new FileWriter(out);
				fw.write(propertiesMap.get(statment));
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					if (fw != null) {
						fw.close();
					}
				} catch (IOException ignoreThisStupidException) {
				}
			}

		}

	}

}
