package com.github.jremoting.registry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryListener;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;

public class ZookeeperRegistry implements Registry {
	
	private final List<RegistryListener> listeners = new CopyOnWriteArrayList<RegistryListener>();
	

	@Override
	public void start() {
		
	}

	@Override
	public void close() {
	
	}

	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {

		return null;
	}

	@Override
	public void publish(ServiceProvider provider) {

	}

	@Override
	public void unpublish(ServiceProvider provider) {
		
	}

	@Override
	public void subscribe(ServiceConsumer consumer) {
		
	}

	@Override
	public void unsubscribe(ServiceConsumer consumer) {
		
	}

	@Override
	public String getAppConfig(String appName, String key) {
		return null;
	}

	@Override
	public String getServiceConfig(String serviceName, String key) {
		return null;
	}

	@Override
	public void addListener(RegistryListener listener) {
		this.listeners.add(listener);
	}
}
