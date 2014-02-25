package com.github.jremoting.registry;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.AbstractRegistryWrapper;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;
import com.github.jremoting.core.RegistryEvent.EventType;
import com.github.jremoting.core.ServiceProvider;


public class CacheRegistryWrapper extends AbstractRegistryWrapper {
	
	private ConcurrentHashMap<String, List<ServiceProvider>> cachedProviders = new ConcurrentHashMap<String, List<ServiceProvider>>();
	private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> cachedAppConfigs = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();
	private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> cachedServiceConfigs = new ConcurrentHashMap<String, ConcurrentHashMap<String,String>>();
	private ConcurrentHashMap<String, String> cachedGlobalConfigs = new ConcurrentHashMap<String, String>();

	public CacheRegistryWrapper(Registry wrappedRegistry) {
		super(wrappedRegistry);
	}
	
	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {
		List<ServiceProvider> providers = cachedProviders.get(invoke.getServiceId());
		if(providers != null) {
			return providers;
		}
		else {
			providers = this.wrappedRegistry.getProviders(invoke);
			cachedProviders.put(invoke.getServiceId(), providers);
			return providers;
		}
	}
	
	@Override
	public String getGlobalConfig(String fileName) {
		String content = cachedGlobalConfigs.get(fileName);
		if(content == null) {
			content = this.wrappedRegistry.getGlobalConfig(fileName);
			cachedGlobalConfigs.put(fileName, content);
		}
		return content;
	}
	
	@Override
	public String getAppConfig(String appName, String fileName) {
		ConcurrentHashMap<String, String> appConfigs = getCachedAppConfigs(appName);
		String content = appConfigs.get(fileName);
		if(content == null) {
			content = this.wrappedRegistry.getAppConfig(appName, fileName);
			appConfigs.put(fileName, content);
		}
		return content;
	}

	@Override
	public String getServiceConfig(String serviceName, String fileName) {
		ConcurrentHashMap<String, String> serviceConifgs = getCachedServiceConfigs(serviceName);
		
		String content = serviceConifgs.get(fileName);
		if(content == null) {
			content = this.wrappedRegistry.getServiceConfig(serviceName, fileName);
			serviceConifgs.put(fileName, content);
		}
		
		return content;
	}
	
	@Override
	public void onEvent(RegistryEvent event) {
		if(event.getType() == EventType.PROVIDERS_CHANGED) {
			cachedProviders.put(event.getServiceId(), event.getNewProviders());
		}
		else if (event.getType() == EventType.GLOBAL_CONFIG_CHANGED) {
			cachedGlobalConfigs.put(event.getFileName(), event.getNewContent());
		}
		else if (event.getType() == EventType.APP_CONFIG_CHANGED) {
			ConcurrentHashMap<String, String> appConfigs = getCachedAppConfigs(event.getAppName());
			appConfigs.put(event.getFileName(), event.getNewContent());
		}
		else if (event.getType() == EventType.SERVICE_CONFIG_CHANGED) {
			ConcurrentHashMap<String, String> serviceConifgs = getCachedServiceConfigs(event.getServiceName());
			serviceConifgs.put(event.getFileName(), event.getNewContent());
		}
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
