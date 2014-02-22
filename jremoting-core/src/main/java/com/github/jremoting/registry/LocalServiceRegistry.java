package com.github.jremoting.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.core.ServiceRegistry;
import com.github.jremoting.group.GroupRule;
import com.github.jremoting.group.GroupStrategy;
import com.github.jremoting.route.RouteRule;
import com.github.jremoting.route.RouteStrategy;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;

public class LocalServiceRegistry implements ServiceRegistry,RemoteRegistryListener {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(LocalServiceRegistry.class);
	
	private ConcurrentHashMap<String, ServiceProvider> localProviders = new ConcurrentHashMap<String, ServiceProvider>();
	private ConcurrentHashMap<String, ServiceConsumer> localConsumers = new ConcurrentHashMap<String, ServiceConsumer>();
	
	private ConcurrentHashMap<String, List<RouteRule>> cachedRouteRules = new ConcurrentHashMap<String, List<RouteRule>>();	
	private ConcurrentHashMap<String, RouteStrategy> cachedRouteStrategies = new ConcurrentHashMap<String, RouteStrategy>();
	private ConcurrentHashMap<String,List<ServiceProvider>> cachedProviders = new ConcurrentHashMap<String, List<ServiceProvider>>();
	private ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceConfig>> cachedConfigs = new ConcurrentHashMap<String, ConcurrentHashMap<String, ServiceConfig>>();
	
	private ConcurrentHashMap<String, Object> serviceLevelWriteLocks = new ConcurrentHashMap<String, Object>();
	
	private RemoteRegistry remoteRegistry;
	
	private String appName;
	private String localIp;

	public LocalServiceRegistry(String appName, String localIp, RemoteRegistry remoteRegistry) {
		this.appName = appName;
		this.localIp = localIp;
		this.remoteRegistry = remoteRegistry;
	}
	
	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {
		try {
			invoke.getConsumer().waitFirstSubscribeFinishIfNeeded();
		} catch (InterruptedException e) {
			Thread.interrupted();
			LOGGER.warn("unexpected wake up from wait subscribe providers", e);
		}
		
		RouteStrategy routeStrategy = cachedRouteStrategies.get(invoke.getServiceName());
		List<ServiceProvider> providers = null;
		if(routeStrategy == null) {
			providers = cachedProviders.get(invoke.getServiceName());
		}
		else {
			providers =  routeStrategy.getProviders(invoke);
		}
		
		if(providers != null) {
			return new ArrayList<ServiceProvider>(providers);
		}
		else {
			return Collections.emptyList();
		}
	}
	
	@Override
	public void publish(ServiceProvider provider) {
		
		ServiceProvider oldProvider = localProviders.putIfAbsent(provider.getServiceName(), provider);
		if(oldProvider != null) {
			return;
		}
		
		doPublish(provider);
	}

	private void doPublish(ServiceProvider provider) {
		List<GroupRule> groupRules = remoteRegistry.getGroupRules(appName, provider.getServiceName());
		
		if(groupRules != null && groupRules.size() > 0) {
			GroupStrategy groupStrategy = new GroupStrategy(groupRules, localIp);
			String newGroup = groupStrategy.getNewGroup(provider);
			provider.setGroup(newGroup);
		}

		remoteRegistry.subscribeConfig(provider.getServiceName());
		
		remoteRegistry.unpublish(provider);
		remoteRegistry.publish(provider);
	}


	@Override
	public void subscribe(ServiceConsumer consumer) {
		
		ServiceConsumer oldConsumer = localConsumers.putIfAbsent(consumer.getServiceName(), consumer);
		if(oldConsumer != null) {
			return;
		}
		
		doSubscribe(consumer);
	}

	private void doSubscribe(ServiceConsumer consumer) {
		
		List<GroupRule> groupRules = remoteRegistry.getGroupRules(appName, consumer.getServiceName());
		
		if(groupRules != null && groupRules.size() > 0) {
			GroupStrategy groupStrategy = new GroupStrategy(groupRules, localIp);
			String newGroup = groupStrategy.getNewGroup(consumer);
			consumer.setGroup(newGroup);
		}
	
		
		remoteRegistry.subscribeRouteRules(appName, consumer.getServiceName());
		remoteRegistry.subscribeConfig(consumer.getServiceName());
		
		remoteRegistry.unsubscribe(consumer);
		remoteRegistry.subscribe(consumer);
	}
	@Override
	public ServiceConfig getConfig(String serviceName, String key) {
		Map<String, ServiceConfig> configs =  cachedConfigs.get(key);
		if(configs!= null) {
			return configs.get(key);
		}
		else {
			return null;
		}
	}
	
	@Override
	public void start() {
		this.remoteRegistry.start();
	}

	@Override
	public void close() {
		for (ServiceConsumer consumer : localConsumers.values()) {
			remoteRegistry.unsubscribe(consumer);
		}
		for (ServiceProvider provider: localProviders.values()) {
			remoteRegistry.unpublish(provider);
		}
		this.remoteRegistry.close();
	}

	public void onGroupRuleChanged(String serviceName, List<GroupRule> newGroupRules) {
		GroupStrategy newGroupStrategy = new GroupStrategy(newGroupRules, serviceName);
	
		for (ServiceConsumer consumer : localConsumers.values()) {
			String newGroup = newGroupStrategy.getNewGroup(consumer);
			if(!newGroup.equals(consumer.getGroup())) {
				remoteRegistry.unsubscribe(consumer);
				consumer.setGroup(newGroup);
				remoteRegistry.subscribe(consumer);
			}
		}
		for (ServiceProvider provider: localProviders.values()) {
			String newGroup = newGroupStrategy.getNewGroup(provider);
			if(!newGroup.equals(provider.getGroup())) {
				remoteRegistry.unpublish(provider);
				provider.setGroup(newGroup);
				remoteRegistry.publish(provider);
			}
		}
	}
	
	public void onProviderChanged(String serviceName, List<ServiceProvider> newProviders) {
		Object writeLock = getServiceLevelWriteLock(serviceName);
		synchronized (writeLock) {
			List<RouteRule> routeRules = cachedRouteRules.get(serviceName);

			RouteStrategy newRouteStrategy = new RouteStrategy(newProviders, routeRules);
			cachedRouteStrategies.put(serviceName, newRouteStrategy);
			
			
			cachedProviders.put(serviceName, newProviders);
			
			ServiceConsumer consumer = localConsumers.get(serviceName);
			consumer.setFirstSubscribeFinished();
		}
		
	}

	private Object getServiceLevelWriteLock(String serviceName) {
		Object writeLock = serviceLevelWriteLocks.get(serviceName);
		if(writeLock == null) {
			serviceLevelWriteLocks.putIfAbsent(serviceName, new Object());
			writeLock = serviceLevelWriteLocks.get(serviceName);
		}
		return writeLock;
	}
	
	public void onRouteRuleChanged(String serviceName, List<RouteRule> newRouteRules) {
		
		Object writeLock = getServiceLevelWriteLock(serviceName);
		
		synchronized (writeLock) {
			List<ServiceProvider> allProviders = cachedProviders.get(serviceName);
			
			RouteStrategy newRouteStrategy = new RouteStrategy(allProviders, newRouteRules);
			cachedRouteStrategies.put(serviceName, newRouteStrategy);
			cachedRouteRules.put(serviceName, newRouteRules);
		}
	}
	
	public void onConfigChanged(String serviceName,String key, String value) {
		
		ConcurrentHashMap<String, ServiceConfig> configs =  cachedConfigs.get(key);
		if(configs == null) {
			configs = new ConcurrentHashMap<String,ServiceConfig>();
			cachedConfigs.putIfAbsent(serviceName, configs);
			configs = cachedConfigs.get(serviceName);
		}
		
		ServiceConfig newConfig = new ServiceConfig();
		ServiceConfig oldConfig = configs.get(key);
		
		if(oldConfig!= null) {
			newConfig.setVersion(oldConfig.getVersion() + 1);
		}
		else {
			newConfig.setVersion(0);
		}
		
		newConfig.setData(value);
		newConfig.setKey(key);
		
		configs.put(key, newConfig);
	}
	
	public void onRecover() {
		for (ServiceConsumer consumer : localConsumers.values()) {
			doSubscribe(consumer);
		}
		
		for (ServiceProvider provider : localProviders.values()) {
			doPublish(provider);
		}
	}
}
