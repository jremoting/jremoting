package com.github.jremoting.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

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
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
import com.github.jremoting.core.ServiceRegistry;
import com.github.jremoting.exception.RegistryExcpetion;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;

public class DefaultServiceRegistry implements ServiceRegistry,
												CuratorListener, 
												ConnectionStateListener,
												UnhandledErrorListener {

	private  Map<String, List<ServiceParticipantInfo>> cachedProviderInfos = new ConcurrentHashMap<String, List<ServiceParticipantInfo>>();
	private List<ServiceParticipantInfo> localParticipantInfos = new ArrayList<ServiceParticipantInfo>();
	
	private  final String zookeeperConnectionString;
	private CuratorFramework client; 
	private CountDownLatch startLatch = new CountDownLatch(1);
	private volatile boolean started = false;
	private volatile boolean closed = false;
	private final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);
	
	public DefaultServiceRegistry(String zookeeperConnectionString) {
		this.zookeeperConnectionString = zookeeperConnectionString;
	}
	
	
	@Override
	public List<ServiceParticipantInfo> getProviders(String serviceName) {
		if (!started) {
			try {
				startLatch.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return cachedProviderInfos.get(serviceName);
	}
	
	@Override
	public void registerParticipant(ServiceParticipantInfo participantInfo) {
		this.localParticipantInfos.add(participantInfo);
	}

	@Override
	public void start() {
		if(started) {
			return;
		}
		synchronized (this) {
			if(started) {
				return;
			}
			
			RetryPolicy retryPolicy = new RetryNTimes(Integer.MAX_VALUE, 1000);
			this.client = CuratorFrameworkFactory.builder()
					.connectString(zookeeperConnectionString)
					.sessionTimeoutMs(10 * 1000).connectionTimeoutMs(15 * 1000)
					.namespace("jremoting").retryPolicy(retryPolicy).build();
			
			//close connection when process exit , let other consumers see this process die immediately
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					DefaultServiceRegistry.this.close();
					logger.info("register closed before process exit!");
				}
			}));
			
			this.client.getCuratorListenable().addListener(this);
			this.client.getConnectionStateListenable().addListener(this);
			this.client.getUnhandledErrorListenable().addListener(this);
			this.client.start();

			
			try {
				this.initLocalParticipantsServicePath();
				this.subscribeProviderInfos();
				this.publishParticipantInfosInBackground();
			} catch (Exception e) {
				throw new RegistryExcpetion(e.getMessage(), e);
			}
			
			this.started = true;
			this.startLatch.countDown();
		}
		
	}
	
	private void subscribeProviderInfos() throws Exception   {
		for (ServiceParticipantInfo participantInfo : localParticipantInfos) {
			if(participantInfo.getType() == ParticipantType.CONSUMER) {
				String providersPath = String.format("/%s/providers", participantInfo.getServiceName());
				List<String> providerJsons = this.client.getChildren().watched().forPath(providersPath);
				refreshProviders(participantInfo.getServiceName(), providerJsons);
			}
		}
	}
	
	private void subscribeProviderInfosInBackground() throws Exception   {
		for (ServiceParticipantInfo participantInfo : localParticipantInfos) {
			if(participantInfo.getType() == ParticipantType.CONSUMER) {
				String providersPath = String.format("/%s/providers", participantInfo.getServiceName());
				this.client.getChildren().watched().inBackground().forPath(providersPath);
			}
		}
	}
	
	private void getProviderInfosInBackground(String changedProvidersPath)
			throws Exception {
		this.client.getChildren().watched().inBackground().forPath(changedProvidersPath);
	}


	private void refreshProviders(String serviceName, List<String> providerJsons) {
		List<ServiceParticipantInfo> providers = new ArrayList<ServiceParticipantInfo>();
		for (String json : providerJsons) {
			providers.add(JSON.parseObject(json, ServiceParticipantInfo.class));
		}
		this.cachedProviderInfos.put(serviceName, providers);
	}
	
	private void initLocalParticipantsServicePath() throws Exception {
		for (ServiceParticipantInfo participantInfo: this.localParticipantInfos) {
			try {
				this.client.inTransaction()
				.create().forPath("/" + participantInfo.getServiceName()).and()
				.create().forPath("/" + participantInfo.getServiceName() + "/providers").and()
				.create().forPath("/" + participantInfo.getServiceName() + "/consumers").and().commit();
			} catch (NodeExistsException e) {
				logger.info("service "+ participantInfo.getServiceName() +" path already exits!");
			}
		}
	}
	
	private void publishParticipantInfosInBackground() throws Exception {
		for (ServiceParticipantInfo participantInfo: this.localParticipantInfos) {
			String  participantPath = null;
			if(participantInfo.getType() == ParticipantType.PROVIDER) {
				participantPath = String.format("/%s/providers/%s", 
						participantInfo.getServiceName(),JSON.toJSONString(participantInfo));
			}
			else {
				participantPath = String.format("/%s/consumers/%s", 
						participantInfo.getServiceName(),JSON.toJSONString(participantInfo));
			}
			
			this.client.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath(participantPath);
		}
	}
	


	@Override
	public void close() {
		if (closed) {
			return;
		}

		synchronized (this) {
			if (closed) {
				return;
			}
			if(this.client != null) {
				this.client.close();
			}
			closed = true;
		}

	}


	@Override
	public void eventReceived(CuratorFramework client, CuratorEvent event)
			throws Exception {
		System.out.println(event);
		switch (event.getType()) {
		case WATCHED:
			if(event.getWatchedEvent().getType() == EventType.NodeChildrenChanged) {
				getProviderInfosInBackground(event.getPath());
			}
			break;
		case CHILDREN:
			if(event.getPath().endsWith("/providers")) {
				refreshProviders(parseServiveName(event.getPath()), event.getChildren());
			}
			break;
		default:
			break;
		}
	}
	
	private String parseServiveName(String changedProvidersPath) {
		return changedProvidersPath.replace("/providers", "").replace("/", "");
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
					this.publishParticipantInfosInBackground();
					this.subscribeProviderInfosInBackground();
				} catch (Exception e) {
					logger.error("republish local providers failed!", e);
				}
			}
			currentState = ConnectionState.RECONNECTED;
		default:
			break;
		}
	}

	@Override
	public void unhandledError(String message, Throwable e) {
		logger.error(message, e);
	}
}
