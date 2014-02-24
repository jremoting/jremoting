package com.github.jremoting.registry;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.ServiceProvider;


public class CacheRegistryWrapper extends AbstractRegistryWrapper {
	
	private ConcurrentHashMap<String, List<ServiceProvider>> cachedProviders = new ConcurrentHashMap<String, List<ServiceProvider>>();
	private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> cachedAppConfigs = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
	private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> cachedServiceConfigs = new ConcurrentHashMap<String, ConcurrentHashMap<String,String>>();
	

	public CacheRegistryWrapper(Registry originalRegistry) {
		super(originalRegistry);
	}
	
	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {
		List<ServiceProvider> providers = cachedProviders.get(invoke.getServiceName());
		if(providers != null) {
			return providers;
		}
		else {
			providers = this.wrappedRegistry.getProviders(invoke);
			cachedProviders.put(invoke.getServiceName(), providers);
			return providers;
		}
	}
	
	@Override
	public String getAppConfig(String appName, String key) {
		ConcurrentHashMap<String, String> appConfigs = getCachedAppConfigs(appName);
		String config = appConfigs.get(key);
		if(key == null) {
		    config = this.wrappedRegistry.getAppConfig(appName, key);
			appConfigs.put(key, config);
		}
		return config;
	}

	@Override
	public String getServiceConfig(String serviceName, String key) {
		ConcurrentHashMap<String, String> serviceConifgs = getCachedServiceConfigs(serviceName);
		
		String config = serviceConifgs.get(key);
		if(config == null) {
			config = this.wrappedRegistry.getServiceConfig(serviceName, key);
			serviceConifgs.put(key, config);
		}
		
		return config;
	}
	
	@Override
	public void onProvidersChanged(String serviceName,
			List<ServiceProvider> newProviders) {
		cachedProviders.put(serviceName, newProviders);
	}
	
	@Override
	public void onAppConfigChanged(String appName, String key, String newValue) {
		ConcurrentHashMap<String, String> appConfigs = getCachedAppConfigs(appName);
		appConfigs.put(key, newValue);
	}

	@Override
	public void onServiceConfigChanged(String serviceName,String key, String newValue) {
		ConcurrentHashMap<String, String> serviceConifgs = getCachedServiceConfigs(serviceName);
		serviceConifgs.put(key, newValue);
	}
	
	private ConcurrentHashMap<String, String> getCachedAppConfigs(String appName) {
		ConcurrentHashMap<String, String> configs = cachedAppConfigs.get(appName);
		if(configs == null) {
			cachedAppConfigs.putIfAbsent(appName, new ConcurrentHashMap<String, String>());
			configs = cachedAppConfigs.get(appName);
		}
		
		return configs;
	}
	
	private ConcurrentHashMap<String, String> getCachedServiceConfigs(String serviceName) {
		ConcurrentHashMap<String, String> configs = cachedServiceConfigs.get(serviceName);
		if(configs == null) {
			cachedServiceConfigs.putIfAbsent(serviceName, new ConcurrentHashMap<String, String>());
			configs = cachedServiceConfigs.get(serviceName);
		}
		
		return configs;
	}
}
