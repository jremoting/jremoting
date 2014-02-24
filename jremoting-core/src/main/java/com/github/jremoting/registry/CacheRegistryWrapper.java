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
	private ConcurrentHashMap<String, String> cachedGlobalConfigs = new ConcurrentHashMap<String, String>();

	public CacheRegistryWrapper(Registry originalRegistry) {
		super(originalRegistry);
	}
	
	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {
		List<ServiceProvider> providers = cachedProviders.get(invoke.getServiceId());
		if(providers != null) {
			return providers;
		}
		else {
			providers = super.getProviders(invoke);
			cachedProviders.put(invoke.getServiceId(), providers);
			return providers;
		}
	}
	
	@Override
	public String getGlobalConfig(String key) {
		String config = cachedGlobalConfigs.get(key);
		if(config == null) {
			config = super.getGlobalConfig(key);
			cachedGlobalConfigs.put(key, config);
		}
		return config;
	}
	
	@Override
	public String getAppConfig(String appName, String key) {
		ConcurrentHashMap<String, String> appConfigs = getCachedAppConfigs(appName);
		String config = appConfigs.get(key);
		if(key == null) {
		    config = super.getAppConfig(appName, key);
			appConfigs.put(key, config);
		}
		return config;
	}

	@Override
	public String getServiceConfig(String serviceName, String key) {
		ConcurrentHashMap<String, String> serviceConifgs = getCachedServiceConfigs(serviceName);
		
		String config = serviceConifgs.get(key);
		if(config == null) {
			config = super.getServiceConfig(serviceName, key);
			serviceConifgs.put(key, config);
		}
		
		return config;
	}
	
	@Override
	public void onProvidersChanged(String serviceId,
			List<ServiceProvider> newProviders) {
		cachedProviders.put(serviceId, newProviders);
	}
	
	@Override
	public void onGlobalConfigChanged(String key, String newValue) {
		cachedGlobalConfigs.put(key, newValue);
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
