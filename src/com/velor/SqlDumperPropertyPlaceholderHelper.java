package com.velor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class SqlDumperPropertyPlaceholderHelper extends
		PropertyPlaceholderConfigurer implements Preprocessor {

	private Map<String, String> propertiesMap;

	public SqlDumperPropertyPlaceholderHelper() {
		super();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void processProperties(
			ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException {
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
	public void preprocess(CommandLine cmd) {

		if (cmd.hasOption("dmpsql")) {

			File dst = new File(cmd.getOptionValue("dmpsql"));
			if (!dst.exists()) {
				dst.mkdirs();
			}

			for (String statment : propertiesMap.keySet()) {
				File out = new File(cmd.getOptionValue("dmpsql") + "/"
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
}
