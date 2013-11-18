package org.easycluster.easyconfig.client.zookeeper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.easycluster.easyconfig.client.DefaultConfigClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZooKeeperConfigClient extends DefaultConfigClient {

	private static final Logger		LOGGER			= LoggerFactory.getLogger(ZooKeeperConfigClient.class);

	private static final String		NODE_SEPARATOR	= "/";

	private volatile ZooKeeper		zooKeeper		= null;
	private volatile ConfigWatcher	watcher			= null;
	private volatile boolean		connected		= false;
	private String					connectString	= "";
	private int						sessionTimeout	= 0;
	private String					rootNode		= "/configuration";
	private String					groupNode		= null;
	private String					dataNode		= null;
	private Lock					lock			= new ReentrantLock();

	public ZooKeeperConfigClient(String group, String dataId, String zooKeeperConnectString, int zooKeeperSessionTimeoutMillis) {
		super(group, dataId);
		this.connectString = zooKeeperConnectString;
		this.sessionTimeout = zooKeeperSessionTimeoutMillis;
		this.groupNode = rootNode + NODE_SEPARATOR + group;
		this.dataNode = groupNode + NODE_SEPARATOR + dataId;
	}

	@Override
	public void start() {
		startZooKeeper();
	}

	private void startZooKeeper() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Connecting to ZooKeeper...");
		}

		try {
			watcher = new ConfigWatcher();
			zooKeeper = new ZooKeeper(connectString, sessionTimeout, watcher);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Connected to ZooKeeper");
			}
		} catch (IOException ex) {
			LOGGER.error("Unable to connect to ZooKeeper", ex);
		} catch (Exception e) {
			LOGGER.error("Exception while connecting to ZooKeeper", e);
		}
	}

	@Override
	public void shutdown() {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling a Shutdown message");
		}

		doWithZooKeeper("shutdown", zooKeeper, new ZooKeeperStatement() {
			public void doInZooKeeper(ZooKeeper zk) throws KeeperException, InterruptedException {
				connected = false;
				watcher.shutdown();
				zooKeeper.close();
			}
		});

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ZooKeeperConfigClient shut down");
		}
	}

	private void handleDataNodeChanged() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("handleClusterEvent");
		}

		String event = "Received configuration changed event";
		if (!connected) {
			LOGGER.error("{} when not connected", event);
			return;
		}

		doWithZooKeeper(event, zooKeeper, new ZooKeeperStatement() {

			public void doInZooKeeper(ZooKeeper zk) throws KeeperException, InterruptedException {
				byte[] data = zk.getData(dataNode, true, null);
				if (data != null && data.length > 0) {
					configNotification.handleConfigurationChanged(fromBytes(data));
				}
			}
		});

	}

	void handleConnected() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("handleConnected");
		}

		String event = "Connected";
		if (connected) {
			LOGGER.error("{} when already connected", event);
			return;
		}
		if (zooKeeper == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignore) {
			}
		}

		doWithZooKeeper(event, zooKeeper, new ZooKeeperStatement() {
			public void doInZooKeeper(ZooKeeper zk) throws KeeperException, InterruptedException {
				verifyZooKeeperStructure(zk);
				byte[] data = zk.getData(dataNode, true, null);
				configNotification.handleConfigurationChanged(fromBytes(data));
				connected = true;
				configNotification.handleConnected();
			}
		});
	}

	private void verifyZooKeeperStructure(ZooKeeper zk) throws KeeperException, InterruptedException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Verifying ZooKeeper structure...");
		}

		for (String path : new String[] { rootNode, groupNode, dataNode }) {
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Ensuring {} exists", path);
				}
				if (zk.exists(path, true) == null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("{} doesn't exist, creating", path);
					}
					zk.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			} catch (NodeExistsException ex) {
				// do nothing
			}
		}
	}

	void handleDisconnected() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("handleDisconnected");
		}

		if (!connected) {
			LOGGER.error("Disconnected when not connected");
			return;
		}

		doWithZooKeeper("Disconnected", zooKeeper, new ZooKeeperStatement() {
			public void doInZooKeeper(ZooKeeper zk) throws KeeperException, InterruptedException {
				connected = false;
				configNotification.handleDisconnected();
			}
		});

	}

	void handleExpired() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("handleExpired");
		}

		LOGGER.error("Connection to ZooKeeper expired, reconnecting...");

		doWithZooKeeper("Expired", zooKeeper, new ZooKeeperStatement() {
			public void doInZooKeeper(ZooKeeper zk) throws KeeperException, InterruptedException {
				connected = false;
				watcher.shutdown();
				startZooKeeper();
			}
		});
	}

	private void doWithZooKeeper(String event, ZooKeeper zk, ZooKeeperStatement action) {
		if (zk == null) {
			LOGGER.error("{} when ZooKeeper is null, this should never happen. ", event);
			return;
		}

		lock.lock();
		try {
			action.doInZooKeeper(zk);
		} catch (KeeperException ex) {
			LOGGER.error("ZooKeeper threw an exception", ex);
		} catch (Exception ex) {
			LOGGER.error("Unhandled exception while working with ZooKeeper", ex);
		} finally {
			lock.unlock();
		}
	}

	private String fromBytes(byte[] bytes) {
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
			LOGGER.error("", ignore);
			throw new RuntimeException(ignore);
		}
	}

	interface ZooKeeperStatement {
		void doInZooKeeper(ZooKeeper zk) throws KeeperException, InterruptedException;
	}

	class ConfigWatcher implements Watcher {
		private volatile boolean	shutdownSwitch	= false;

		public void process(WatchedEvent event) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Received watched event {}", ToStringBuilder.reflectionToString(event));
			}

			if (shutdownSwitch) {
				return;
			}

			if (event.getType() == EventType.None) {
				if (event.getState() == KeeperState.SyncConnected) {
					handleConnected();
				} else if (event.getState() == KeeperState.Expired) {
					handleExpired();
				} else if (event.getState() == KeeperState.Disconnected) {
					handleDisconnected();
				}
			} else if (event.getType() == EventType.NodeDataChanged) {
				if (event.getPath().equals(dataNode)) {
					handleDataNodeChanged();
				} else {
					LOGGER.error("Received a notification for a path that shouldn't be monitored: {}", event.getPath());
				}
			}

		}

		public void shutdown() {
			shutdownSwitch = true;
		}
	}

}
