package com.github.jremoting.registry;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;
import com.github.jremoting.core.RegistryListener;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.exception.RegistryExcpetion;
import com.github.jremoting.util.LifeCycleSupport;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;

public class ZookeeperRegistry implements Registry, CuratorListener,
		ConnectionStateListener, UnhandledErrorListener {
	
	private  CuratorFramework client; 
	private final LifeCycleSupport lifeCycleSupport = new LifeCycleSupport();
	private final String zookeeperConnectionString;
	private final List<RegistryListener> listeners = new CopyOnWriteArrayList<RegistryListener>();
	protected RegistryPathManager pathManager;
	private final static Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistry.class);
	
	
	private Map<String, ServiceProvider> localProviders = new ConcurrentHashMap<String, ServiceProvider>();
	protected Set<ServiceProvider> publishedProviders = new CopyOnWriteArraySet<ServiceProvider>();
	protected Set<ServiceConsumer> subcribedConsumers = new CopyOnWriteArraySet<ServiceConsumer>();
	
	private Set<String> watchedFiles = new CopyOnWriteArraySet<String>();
	
	ConcurrentHashMap<String, EnsurePath> ensurePaths = new ConcurrentHashMap<String, EnsurePath>();
	
	
	

	public ZookeeperRegistry(String zookeeperConnectionString) {
		this.zookeeperConnectionString = zookeeperConnectionString;
		pathManager = new RegistryPathManager();
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
				.namespace(pathManager.getNamespace()).retryPolicy(retryPolicy).build();
		//close connection when process exit , let other consumers see this process die immediately
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				ZookeeperRegistry.this.close();
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
				ZookeeperRegistry.this.client.close();
				LOGGER.info("service register closed before process exit!");
			}
		});
	}


	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {
		String providerDir = pathManager.getProviderDir(invoke.getConsumer());
		
		List<String> providerFileNames = this.getChildrenAndWatched(providerDir);
		
		List<ServiceProvider> providers = new ArrayList<ServiceProvider>(providerFileNames.size());
		for (String fileName : providerFileNames) {
			ServiceProvider provider = pathManager.decode(fileName);
			providers.add(provider);
		}
		
		return providers;
	}
	


	@Override
	public void publish(ServiceProvider provider) {
		
		republish(provider);
		
		publishedProviders.add(provider);
	}

	private void republish(ServiceProvider provider) {

		localProviders.put(provider.getServiceId(), provider);
		
		String dir = pathManager.getProviderDir(provider);
		this.ensurePath(dir);
		
		String fileName = pathManager.encode(provider);
		String filePath = dir + "/" + fileName;
		
		this.deleteFile(filePath);
		
		this.createEphemeralFile(filePath);
		
	}

	@Override
	public void unpublish(ServiceProvider provider) {
		String dir = pathManager.getProviderDir(provider);
		String fileName = pathManager.encode(provider);
		
		String filePath = dir + "/" + fileName;
		this.deleteFile(filePath);
		
		publishedProviders.remove(provider);
		
	}

	@Override
	public void subscribe(ServiceConsumer consumer) {
		
		resubscribe(consumer);
		
		subcribedConsumers.add(consumer);
	}

	private void resubscribe(ServiceConsumer consumer) {
		String dir = pathManager.getConsumerDir(consumer);
		this.ensurePath(dir);
		String fileName = pathManager.encode(consumer);
		
		String filePath = dir + "/" + fileName;
		this.deleteFile(filePath);
		this.createEphemeralFile(filePath);
	}

	@Override
	public void unsubscribe(ServiceConsumer consumer) {
		String dir = pathManager.getConsumerDir(consumer);
		String fileName = pathManager.encode(consumer);
		
		String filePath = dir + "/" + fileName;
		this.deleteFile(filePath);
		subcribedConsumers.remove(consumer);
	}

	@Override
	public String getAppConfig(String appName, String fileName) {
		String appDir = pathManager.getAppConfigDir() + appName;
		String configFile = appDir + "/"  + fileName;
		watchedFiles.add(configFile);
		return this.getDataAndWatched(configFile);
	}

	@Override
	public String getServiceConfig(String serviceName, String fileName) {
		String serviceDir = pathManager.getServiceConfigDir() + serviceName;
		String configFile = serviceDir + "/" + fileName;
		watchedFiles.add(configFile);
		return this.getDataAndWatched(configFile);
	}

	@Override
	public void addListener(RegistryListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public String getGlobalConfig(String fileName) {
		String glocalDir = pathManager.getGlobalConfigDir();
		String configFile = glocalDir + fileName;
		watchedFiles.add(configFile);
		return this.getDataAndWatched(configFile);
	}
	
	@Override
	public void eventReceived(CuratorFramework client, CuratorEvent event)
			throws Exception {
		
		switch (event.getType()) {
		case WATCHED:
			if(event.getWatchedEvent().getType() == EventType.NodeDataChanged) {
				getDataAndWatchedInBackgroud(event.getPath());
			}
			if(event.getWatchedEvent().getType() == EventType.NodeChildrenChanged) {
				getChildrenAndWatchedInBackgroud(event.getPath());
			}
			break;
		case CHILDREN:
			handleChildrenChangedEvent(event);
			break;
		case GET_DATA:
			handleDataChangedEvent(event);
			break;
		default:
			break;
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(event.toString());
		}
	}

	private ConnectionState prevConnectionState;
	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
		
		if(prevConnectionState == ConnectionState.LOST && newState == ConnectionState.RECONNECTED) {
			try {
				this.recover();
			} catch (Exception e) {
				LOGGER.error("republish local participant failed!", e);
			}
		}
		
		prevConnectionState = newState;
	}
	
	@Override
	public void unhandledError(String message, Throwable e) {
		LOGGER.error(message, e);
	}
	
	
	protected void handleChildrenChangedEvent(CuratorEvent event) {
		
		
		Map<String, List<ServiceProvider>> changedProvidersMap = new HashMap<String, List<ServiceProvider>>();
		
		for (int i = 0; i < event.getChildren().size(); i++) {
			String fileName = event.getChildren().get(i);
			ServiceProvider provider = pathManager.decode(fileName);
			List<ServiceProvider> providers = changedProvidersMap.get(provider.getServiceId());
			if(providers == null) {
				providers = new ArrayList<ServiceProvider>();
				changedProvidersMap.put(provider.getServiceId(), providers);
			}
			providers.add(provider);
		}
		
		for (String serviceId : changedProvidersMap.keySet()) {
			RegistryEvent registryEvent = new RegistryEvent();
			registryEvent.setServiceId(serviceId);
			registryEvent.setNewProviders(changedProvidersMap.get(serviceId));
			registryEvent.setType(com.github.jremoting.core.RegistryEvent.EventType.PROVIDERS_CHANGED);
			
			for(RegistryListener listener : listeners) {
				listener.onEvent(registryEvent);
			}
		}
		
	}
	
	private void handleDataChangedEvent(CuratorEvent event) {
		
		if(event.getPath().startsWith(pathManager.getGlobalConfigDir())) {
			String fileName = event.getPath().replace(pathManager.getGlobalConfigDir(), "");
			String newContent = bytesToString(event.getData());
			RegistryEvent registryEvent = new RegistryEvent();
			registryEvent.setFileName(fileName);
			registryEvent.setNewContent(newContent);
			registryEvent.setType(com.github.jremoting.core.RegistryEvent.EventType.GLOBAL_CONFIG_CHANGED);
			for(RegistryListener listener : listeners) {
				listener.onEvent(registryEvent);
			}
		}
		if(event.getPath().startsWith(pathManager.getAppConfigDir())) {
			String[] paths = event.getPath().split("/");
			String appName = paths[2];
			String fileName = paths[3];
			String newContent = bytesToString(event.getData());
			RegistryEvent registryEvent = new RegistryEvent();
			registryEvent.setAppName(appName);
			registryEvent.setFileName(fileName);
			registryEvent.setNewContent(newContent);
			registryEvent.setType(com.github.jremoting.core.RegistryEvent.EventType.APP_CONFIG_CHANGED);
			
			for(RegistryListener listener : listeners) {
				listener.onEvent(registryEvent);
			}
		}
		if(event.getPath().startsWith(pathManager.getServiceConfigDir())) {
			String[] paths = event.getPath().split("/");
			String serviceName = paths[2];
			String fileName = paths[3];
			String newContent = bytesToString(event.getData());
			RegistryEvent registryEvent = new RegistryEvent();
			registryEvent.setServiceName(serviceName);
			registryEvent.setFileName(fileName);
			registryEvent.setNewContent(newContent);
			registryEvent.setType(com.github.jremoting.core.RegistryEvent.EventType.SERVICE_CONFIG_CHANGED);
			for(RegistryListener listener : listeners) {
				listener.onEvent(registryEvent);
			}
		}
	}
	
	private void recover() {
		
		for (ServiceProvider provider : publishedProviders) {
			republish(provider);
		}
		
		for (ServiceConsumer consumer : subcribedConsumers) {
			resubscribe(consumer);
		}
		
		for (String watchedFile : watchedFiles) {
			getDataAndWatchedInBackgroud(watchedFile);
		}
	}
	
	
	private void ensurePath(String path) {
		path = "/" + pathManager.getNamespace() + path;
		EnsurePath ensurePath = ensurePaths.get(path);
		if(ensurePath == null) {
			ensurePaths.putIfAbsent(path, new EnsurePath(path));
			ensurePath = ensurePaths.get(path);
		}
		
		try {
			ensurePath.ensure(this.client.getZookeeperClient());
		} catch (Exception e) {
			throw new RegistryExcpetion("ensure path failed for path:" + path, e);
		}
	}
	
	private String bytesToString(byte[] data) {
		try {
			return new String(data,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RegistryExcpetion("should not happen" , e);
		}
	}



	
	
	private List<String> getChildrenAndWatched(String dir) {
		try {
			return this.client.getChildren().watched().forPath(dir);
		} catch (Exception e) {
			throw new RegistryExcpetion("get children failed for dir:" + dir, e);
		}
	}
	
	private void getChildrenAndWatchedInBackgroud(String dir) {
		try {
		   this.client.getChildren().watched().inBackground().forPath(dir);
		} catch (Exception e) {
			throw new RegistryExcpetion("get children failed for dir:" + dir, e);
		}
	}
	
	private void createEphemeralFile(String filePath) {
		try {
			this.client.create().withMode(CreateMode.EPHEMERAL).inBackground().forPath(filePath);
		} catch (Exception e) {
			throw new RegistryExcpetion("create ephemeral file failed for path:" + filePath, e);
		}
	}
	
	private void deleteFile(String filePath) {
		try {
			this.client.delete().inBackground().forPath(filePath);
		} catch (Exception e) {
			throw new RegistryExcpetion("delete file failed for path:" + filePath, e);
		}
	}
	
	private String getDataAndWatched(String filePath) {
		try {
			ensurePath(filePath);
			byte[] data = this.client.getData().watched().forPath(filePath);
			return bytesToString(data);
		} catch (Exception e) {
			throw new RegistryExcpetion("get data failed for path:" + filePath, e);
		}
	}
	
	private void getDataAndWatchedInBackgroud(String filePath) {
		 try {
			ensurePath(filePath);
			this.client.getData().watched().inBackground().forPath(filePath);
		} catch (Exception e) {
			throw new RegistryExcpetion("get data failed for path:" + filePath, e);
		}
	}

	@Override
	public Map<String, ServiceProvider> getLocalProviders() {
		return localProviders;
	}
}
