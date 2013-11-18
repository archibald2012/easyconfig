package org.easycluster.easyconfig.client;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesConfigListener implements ConfigListener {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(PropertiesConfigListener.class);

	@Override
	public void handleConfigurationChange(String configInfo) {
		if (configInfo == null || configInfo.length() == 0) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("The received new config info is null or empty.");
			}
			return;
		}

		Properties properties = new Properties();
		try {
			properties.load(new StringReader(configInfo));
			// innerReceive(properties);
		} catch (IOException e) {
			LOGGER.error("Failed to read properties：" + configInfo, e);
			throw new RuntimeException(e);
		}
	}

}
