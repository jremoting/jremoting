package com.github.jremoting.registry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceParticipant;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.exception.RegistryExcpetion;
import com.github.jremoting.group.GroupRule;
import com.github.jremoting.util.LifeCycleSupport;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;

public class DefaultRemoteRegistry implements RemoteRegistry,
		CuratorListener, ConnectionStateListener, UnhandledErrorListener {


	private final LifeCycleSupport lifeCycleSupport = new LifeCycleSupport();
	protected  final ServicePathCodec pathCodec;
	private final String zookeeperConnectionString;
	private  CuratorFramework client; 
	private final static Logger LOGGER = LoggerFactory.getLogger(DefaultRemoteRegistry.class);
	
	private List<RemoteRegistryListener> remoteRegistryListeners = new CopyOnWriteArrayList<RemoteRegistryListener>();


	ConcurrentHashMap<String, ServiceParticipant> initedParticipants = new ConcurrentHashMap<String, ServiceParticipant>();
	
	private InMemoryServiceRegistry localServiceRegistry;

	public DefaultRemoteRegistry(String zookeeperConnectionString,ServicePathCodec pathCodec) {
		this.zookeeperConnectionString = zookeeperConnectionString;
		this.pathCodec = pathCodec;
	}
	
	private void initServicePath(ServiceParticipant participant)  {
		try {
			String[] dirs = pathCodec.getServiceDirs(participant.getServiceName());
			for (int i = 0; i < dirs.length; i++) {
				this.client.create().inBackground().forPath(dirs[i]);
			}
		} catch (Exception e) {
			throw new RegistryExcpetion(e.getMessage(), e);
		}
	}
	
	@Override
	public void publish(ServiceProvider provider) {
		this.start();
		ServiceParticipant oldParticipant = initedParticipants.putIfAbsent(provider.getServiceName(), provider);
		if(oldParticipant == null) {
			initServicePath(provider);
		}
		try {
			String  participantPath = pathCodec.toServicePath(provider);
			this.client.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath(participantPath);
			LOGGER.info("publish service to path:" + participantPath);
		} catch (Exception e) {
			throw new RegistryExcpetion(e.getMessage(), e);
		}
	}
	
	@Override
	public void unpublish(ServiceProvider provider) {
		this.start();
		try { 
			//delete path that previous session created 
			this.client.delete().inBackground().forPath(pathCodec.toServicePath(provider));
			LOGGER.info("unpublish service:" + provider.getServiceName());
		} catch (Exception e) {
			throw new RegistryExcpetion(e.getMessage(), e);
		}
	}

	@Override
	public void subscribe(ServiceConsumer consumer) {
		this.start();
		
		ServiceParticipant oldParticipant = initedParticipants.putIfAbsent(consumer.getServiceName(), consumer);
		if(oldParticipant == null) {
			initServicePath(consumer);
		}
		try {
			this.client.getChildren().watched().inBackground().forPath(pathCodec.toServicePath(consumer));
			LOGGER.info("subscribe service:" + consumer.getServiceName());
		} catch (Exception e) {
			throw new RegistryExcpetion(e.getMessage(), e);
		}
	}
	@Override
	public void unsubscribe(ServiceConsumer consumer) {
		this.start();
		try { 
			//delete path that previous session created 
			this.client.delete().inBackground().forPath(pathCodec.toServicePath(consumer));
			LOGGER.info("unsubscribe service:" + consumer.getServiceName());
		} catch (Exception e) {
			throw new RegistryExcpetion(e.getMessage(), e);
		}
	}

	@Override
	public void unhandledError(String message, Throwable e) {
		LOGGER.error(message, e);

	}

	private ConnectionState currentState;

	
	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
		switch (newState) {
		case CONNECTED:
			currentState = ConnectionState.CONNECTED;
		case LOST:
			currentState = ConnectionState.LOST;
			break;
		case RECONNECTED:
			if (currentState == ConnectionState.LOST) {
				try {
					localServiceRegistry.onRecover();
				} catch (Exception e) {
					LOGGER.error("republish local participant failed!", e);
				}
			}
			currentState = ConnectionState.RECONNECTED;
		default:
			break;
		}
	}

	@Override
	public void eventReceived(CuratorFramework client, CuratorEvent event)
			throws Exception {
		switch (event.getType()) {
		case WATCHED:
			if(event.getWatchedEvent().getType() == EventType.NodeChildrenChanged) {
				//localServiceRegistry.onProviderChanged(serviceName, newProviders);
			}
			break;
		case CHILDREN:
			if(event.getPath().endsWith("/providers")) {
				
			}
			break;
		default:
			break;
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(event.toString());
		}

	}

	@Override
	public void start() {
		lifeCycleSupport.start(new Runnable() {
			@Override
			public void run() {
				doStart();
			}
		});
	}

	private void doStart() {
		RetryPolicy retryPolicy = new RetryNTimes(Integer.MAX_VALUE, 1000);
		this.client = CuratorFrameworkFactory.builder()
				.connectString(zookeeperConnectionString)
				.sessionTimeoutMs(5 * 1000).connectionTimeoutMs(5 * 1000)
				.namespace(pathCodec.getRootPath()).retryPolicy(retryPolicy).build();
		//close connection when process exit , let other consumers see this process die immediately
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				DefaultRemoteRegistry.this.close();
			}
		}));
		
		this.client.getCuratorListenable().addListener(this);
		this.client.getConnectionStateListenable().addListener(this);
		this.client.getUnhandledErrorListenable().addListener(this);
		this.client.start();
	}

	@Override
	public void close() {
		lifeCycleSupport.close(new Runnable() {
			@Override
			public void run() {
				DefaultRemoteRegistry.this.client.close();
			}
		});
	}

	@Override
	public void subscribeConfig(String serviceName) {
		this.start();
	}

	@Override
	public List<GroupRule> getGroupRules(String appName,
			String serviceName) {
		this.start();
		return null;
	}

	@Override
	public void subscribeRouteRules(String appName, String serviceName) {
		
	}

	@Override
	public void addListener(RemoteRegistryListener listener) {
		remoteRegistryListeners.add(listener);
	}
}
