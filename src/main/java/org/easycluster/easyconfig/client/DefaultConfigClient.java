package org.easycluster.easyconfig.client;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConfigClient implements ConfigClient {

	private static final Logger		LOGGER				= LoggerFactory.getLogger(DefaultConfigClient.class);

	private String					group				= null;
	private String					dataId				= null;
	private AtomicBoolean			shutdownSwitch		= new AtomicBoolean(false);
	private volatile CountDownLatch	connectedLatch		= new CountDownLatch(1);
	
	protected ConfigNotification	configNotification	= null;

	public DefaultConfigClient(String group, String dataId) {
		this.group = group;
		this.dataId = dataId;
		this.configNotification = new DefaultConfigNotification();
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public String getDataId() {
		return dataId;
	}

	@Override
	public String getConfigurationInfo() {
		return configNotification.getCurrentConfiguration();
	}

	@Override
	public String getConfigurationInfo(long timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getConfiguratonInfoFromSnapshot(long timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getPropertiesConfigurationInfo(long timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getPropertiesConfigurationInfoFromSnapshot(long timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long addListener(ConfigListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Listener is null.");
		}

		return configNotification.handleAddListener(listener);
	}

	@Override
	public void removeListener(Long key) {
		if (key == null) {
			throw new IllegalArgumentException("Listener key is null.");
		}

		configNotification.handleRemoveListener(key);
	}

	@Override
	public boolean isConnected() {
		return connectedLatch.getCount() == 0;
	}

	@Override
	public boolean isShutdown() {
		return shutdownSwitch.get();
	}

	@Override
	public void awaitConnection() throws InterruptedException {
		connectedLatch.await();
	}

	@Override
	public boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException {
		return connectedLatch.await(timeout, unit);
	}

	@Override
	public void awaitConnectionUninterruptibly() {
		boolean completed = false;

		while (!completed) {
			try {
				awaitConnection();
				completed = true;
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		if (shutdownSwitch.compareAndSet(false, true)) {

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Shutting down configClient...");
			}

			// config.shutdown();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Config client shut down");
			}
		}
	}

}
