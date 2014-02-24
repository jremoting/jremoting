package com.github.jremoting.registry;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryListener;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;

public abstract class AbstractRegistryWrapper implements Registry , RegistryListener {
	
	private  Registry wrappedRegistry;
	
	protected Set<ServiceProvider> publishedProviders = new CopyOnWriteArraySet<ServiceProvider>();
	protected Set<ServiceConsumer> subcribedConsumers = new CopyOnWriteArraySet<ServiceConsumer>();
	
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
	public void publish(ServiceProvider provider) {
		this.wrappedRegistry.publish(provider);
		publishedProviders.add(provider);
	}

	@Override
	public void unpublish(ServiceProvider provider) {
		this.wrappedRegistry.unpublish(provider);
		publishedProviders.remove(provider);
	}

	@Override
	public void subscribe(ServiceConsumer consumer) {
		this.wrappedRegistry.subscribe(consumer);
		subcribedConsumers.add(consumer);
	}

	@Override
	public void unsubscribe(ServiceConsumer consumer) {
		this.wrappedRegistry.unsubscribe(consumer);
		subcribedConsumers.remove(consumer);
	}

	@Override
	public String getAppConfig(String appName, String key) {
		return this.wrappedRegistry.getAppConfig(appName, key);
	}

	@Override
	public String getServiceConfig(String serviceName, String key) {
		return this.wrappedRegistry.getServiceConfig(serviceName, key);
	}
	@Override
	public String getGlobalConfig(String key) {
		return this.wrappedRegistry.getGlobalConfig(key);
	}

	@Override
	public void addListener(RegistryListener listener) {
		this.wrappedRegistry.addListener(listener);
	}
	
	@Override
	public void onProvidersChanged(String serviceId,
			List<ServiceProvider> newProviders) {
	}

	@Override
	public void onAppConfigChanged(String appName, String key, String newValue) {

	}

	@Override
	public void onServiceConfigChanged(String serviceName,String key, String newValue) {

	}
	@Override
	public void onGlobalConfigChanged(String key, String newValue) {
		
	}
	
	@Override
	public void onRecover() {
		
	}

}
