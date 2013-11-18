package org.easycluster.easyconfig.client;

public interface ConfigNotification {

	Long handleAddListener(ConfigListener listener);

	void handleRemoveListener(Long key);

	void handleConnected();

	void handleDisconnected();

	void handleConfigurationChanged(String configuration);

	String getCurrentConfiguration();
}
