package com.github.jremoting.registry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.apache.zookeeper.Watcher.Event.EventType;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.core.ServiceParticipantInfo.ParticipantType;
import com.github.jremoting.core.ServiceRegistry;
import com.github.jremoting.exception.RegistryExcpetion;
import com.github.jremoting.util.LifeCycleSupport;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;

public class DefaultServiceRegistry implements ServiceRegistry,
												CuratorListener, 
												ConnectionStateListener,
												UnhandledErrorListener {

	private final Map<String, List<ServiceParticipantInfo>> cachedProviderInfos = new ConcurrentHashMap<String, List<ServiceParticipantInfo>>();
	private final ConcurrentHashMap<String, CountDownLatch> initSubscribeLatches = new ConcurrentHashMap<String, CountDownLatch>();
	private final List<ServiceParticipantInfo> localParticipantInfos = new CopyOnWriteArrayList<ServiceParticipantInfo>();
	
	private  CuratorFramework client; 
	private final LifeCycleSupport lifeCycleSupport = new LifeCycleSupport();
	
	protected  ServicePathCodec codec;
	private final String zookeeperConnectionString;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(DefaultServiceRegistry.class);
	
	public DefaultServiceRegistry(String zookeeperConnectionString) {
		this.codec = new ServicePathCodec();
		this.zookeeperConnectionString = zookeeperConnectionString;
	}
	
	
	@Override
	public List<ServiceParticipantInfo> getProviders(String serviceName) {
		
		List<ServiceParticipantInfo> providers = cachedProviderInfos.get(serviceName);
		if(providers != null) {
			return providers;
		}
		
		//if no provider then wait for first async subscribe action to complete and query again
		CountDownLatch subscribeLatch = initSubscribeLatches.get(serviceName);
		
		if(subscribeLatch == null) {
			throw new RegistryExcpetion("serviceName " + serviceName + " is not register to subscribe providers!");
		}
		
		try {
			subscribeLatch.await();
		} catch (InterruptedException e) {
			throw new RegistryExcpetion(e.getMessage(), e);
		}
		
		return cachedProviderInfos.get(serviceName);
	}
	
	@Override
	public void registerParticipant(ServiceParticipantInfo participantInfo) {
		
		if(this.localParticipantInfos.contains(participantInfo)) {
			return;
		}
		
		this.localParticipantInfos.add(participantInfo);
		
		this.start();
		this.initServicePath(participantInfo);
		this.publish(participantInfo);
		if (participantInfo.getType() == ParticipantType.CONSUMER) {
			this.initSubscribeLatches.put(participantInfo.getServiceName(), new CountDownLatch(1));
			this.subscribe(codec.toProvidersDir(participantInfo.getServiceName()));
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
				.namespace(codec.getRootPath()).retryPolicy(retryPolicy).build();
		//close connection when process exit , let other consumers see this process die immediately
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				DefaultServiceRegistry.this.close();
			}
		}));
		
		this.client.getCuratorListenable().addListener(this);
		this.client.getConnectionStateListenable().addListener(this);
		this.client.getUnhandledErrorListenable().addListener(this);
		this.client.start();
	}
	
	
	private void subscribe(String changedProvidersPath) {
		try {
			this.client.getChildren().watched().inBackground().forPath(changedProvidersPath);
		} catch (Exception e) {
			throw new RegistryExcpetion(e.getMessage(), e);
		}
	}


	private void refreshCachedProviders(String changedParentPath, List<String> providerFileNames) {
		Map<String, List<ServiceParticipantInfo>> changedProviders = codec.parseChangedProviderPath(changedParentPath, providerFileNames);
		
		for (String serviceName : changedProviders.keySet()) {
			List<ServiceParticipantInfo> providers = changedProviders.get(serviceName);
			
			this.cachedProviderInfos.put(serviceName, providers);
			//if there are consumer threads blocked to wait init subscribe  then wake up them
			if(this.initSubscribeLatches.size() > 0) {
				CountDownLatch subscribeLatch = this.initSubscribeLatches.remove(serviceName);
				if(subscribeLatch != null) {
					subscribeLatch.countDown();
				}
			}
			LOGGER.info("received providers for service:" + serviceName);
			LOGGER.info("providers:" + JSON.toJSONString(providers));
		}
		
	}
	

	private void initServicePath(ServiceParticipantInfo participantInfo)  {
		try {
			String[] dirs = codec.getServiceDirs(participantInfo.getServiceName());
			for (int i = 0; i < dirs.length; i++) {
				this.client.create().inBackground().forPath(dirs[i]);
				
			}
		} catch (Exception e) {
			throw new RegistryExcpetion(e.getMessage(), e);
		}
	}


	private void publish(ServiceParticipantInfo participantInfo)  {
		
		try {
			String  participantPath = codec.toServicePath(participantInfo);
			this.client.delete().inBackground().forPath(participantPath); //delete path that previous session created  
			this.client.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath(participantPath);
			LOGGER.info("publish participant to path:" + participantPath);
		} catch (Exception e) {
			throw new RegistryExcpetion(e.getMessage(), e);
		}
	}
	


	@Override
	public void close() {
		lifeCycleSupport.close(new Runnable() {
			@Override
			public void run() {
				DefaultServiceRegistry.this.client.close();
				LOGGER.info("service register closed before process exit!");
			}
		});
	}


	@Override
	public void eventReceived(CuratorFramework client, CuratorEvent event)
			throws Exception {
		
		switch (event.getType()) {
		case WATCHED:
			if(event.getWatchedEvent().getType() == EventType.NodeChildrenChanged) {
				subscribe(event.getPath());
			}
			break;
		case CHILDREN:
			if(event.getPath().endsWith("/providers")) {
				refreshCachedProviders(event.getPath(), event.getChildren());
			}
			break;
		default:
			break;
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(event.toString());
		}
	}
	

	private ConnectionState currentState;
	
	private void recover() throws Exception   {
		for (ServiceParticipantInfo participantInfo: this.localParticipantInfos) {
			if(participantInfo.getType() == ParticipantType.CONSUMER) {
				subscribe(codec.toProvidersDir(participantInfo.getServiceName()));
			}
			publish(participantInfo);
		}
	}

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
					this.recover();
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
	public void unhandledError(String message, Throwable e) {
		LOGGER.error(message, e);
	}
}
