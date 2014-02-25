package com.github.jremoting.core;

import java.util.List;
import java.util.Map;


public abstract class AbstractRegistryWrapper implements Registry , RegistryListener {
	
	protected  Registry wrappedRegistry;
	
	
	public AbstractRegistryWrapper(Registry wrappedRegistry) {
		this.wrappedRegistry = wrappedRegistry;
		this.wrappedRegistry.addListener(this);
	}

	@Override
	public void start() {
		this.wrappedRegistry.start();
	}

	@Override
	public void close() {
		this.wrappedRegistry.close();
	}

	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {
		return this.wrappedRegistry.getProviders(invoke);
	}
	@Override
	public Map<String, ServiceProvider> getLocalProviders() {
		return this.wrappedRegistry.getLocalProviders();
	}

	@Override
	public void publish(ServiceProvider provider) {
		this.wrappedRegistry.publish(provider);
		
	}

	@Override
	public void unpublish(ServiceProvider provider) {
		this.wrappedRegistry.unpublish(provider);
	}

	@Override
	public void subscribe(ServiceConsumer consumer) {
		this.wrappedRegistry.subscribe(consumer);
	
	}

	@Override
	public void unsubscribe(ServiceConsumer consumer) {
		this.wrappedRegistry.unsubscribe(consumer);
	}

	@Override
	public String getAppConfig(String appName, String fileName) {
		return this.wrappedRegistry.getAppConfig(appName, fileName);
	}

	@Override
	public String getServiceConfig(String serviceName, String fileName) {
		return this.wrappedRegistry.getServiceConfig(serviceName, fileName);
	}
	@Override
	public String getGlobalConfig(String fileName) {
		return this.wrappedRegistry.getGlobalConfig(fileName);
	}

	@Override
	public void addListener(RegistryListener listener) {
		this.wrappedRegistry.addListener(listener);
	}
	
	@Override
	public void onEvent(RegistryEvent event) {
		
	}

}
