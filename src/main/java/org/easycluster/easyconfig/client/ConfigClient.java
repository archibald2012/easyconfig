package org.easycluster.easyconfig.client;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public interface ConfigClient {

	/**
	 * Retrieves the name of the group running on this configuration cluster
	 * 
	 * @return the name of the group running on this cluster
	 */
	String getGroup();

	/**
	 * Retrieves the name of the data id running on this configuration cluster
	 * 
	 * @return the name of the data id running on this cluster
	 */
	String getDataId();

	/**
	 * Looks up the configuration info.
	 * 
	 * @return the configuration information if found, otherwise null
	 */
	String getConfigurationInfo();

	String getConfigurationInfo(long timeout);

	String getConfiguratonInfoFromSnapshot(long timeout);

	Properties getPropertiesConfigurationInfo(long timeout);

	Properties getPropertiesConfigurationInfoFromSnapshot(long timeout);

	/**
	 * Registers a <code>ConfigListener</code> with the
	 * <code>ConfigClient</code> to receive cluster events.
	 * 
	 * @param listener
	 *            the listener instance to register
	 * 
	 * @return a ConfigListenerKey that can be used to unregister the listener
	 */
	Long addListener(ConfigListener listener);

	/**
	 * Unregisters a <code>ConfigListener</code> with the
	 * <code>ConfigClient</code>.
	 * 
	 * @param key
	 *            the key what was returned by <code>addListener</code> when the
	 *            <code>ConfigListener</code> was registered
	 */
	void removeListener(Long key);

	/**
	 * Queries whether or not a connection to the cluster is established.
	 * 
	 * @return true if connected, false otherwise
	 */
	boolean isConnected();

	/**
	 * Queries whether or not this <code>ConfigClient</code> has been shut
	 * down.
	 * 
	 * @return true if shut down, false otherwise
	 */
	boolean isShutdown();

	/**
	 * Waits for the connection to the cluster to be established. This method
	 * will wait indefinitely for the connection.
	 * 
	 * @throws InterruptedException
	 *             thrown if the current thread is interrupted while waiting
	 */
	void awaitConnection() throws InterruptedException;

	/**
	 * Waits for the connection to the cluster to be established for the
	 * specified duration of time.
	 * 
	 * @param timeout
	 *            how long to wait before giving up, in terms of
	 *            <code>unit</code>
	 * @param unit
	 *            the <code>TimeUnit</code> that <code>timeout</code> should be
	 *            interpreted in
	 * 
	 * @return true if the connection was established before the timeout, false
	 *         if the timeout occurred
	 * @throws InterruptedException
	 *             thrown if the current thread is interrupted while waiting
	 */
	boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * Waits for the connection to the cluster to be established. This method
	 * will wait indefinitely for the connection and will swallow any
	 * <code>InterruptedException</code>s thrown while waiting.
	 */
	void awaitConnectionUninterruptibly();

	/**
	 * start up the cluster configuration client.
	 */
	void start();

	/**
	 * Shut down the cluster configuration client.
	 */
	void shutdown();
}
