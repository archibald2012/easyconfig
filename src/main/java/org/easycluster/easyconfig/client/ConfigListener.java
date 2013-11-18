package org.easycluster.easyconfig.client;

public interface ConfigListener {

	/**
	 * 
	 * @param configInfo
	 */
	void handleConfigurationChange(final String configInfo);
}
