package com.github.jremoting.registry.test;

import java.util.List;
import java.util.Map;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryListener;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;

public abstract class MockRegistry implements Registry {

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
	public Map<String, ServiceProvider> getLocalProviders() {
		return null;
	}

	@Override
	public String getGlobalConfig(String fileName) {
		return null;
	}

	@Override
	public String getAppConfig(String appName, String fileName) {
		return null;
	}

	@Override
	public String getServiceConfig(String serviceName, String fileName) {
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
	public void addListener(RegistryListener listener) {
	}

}
