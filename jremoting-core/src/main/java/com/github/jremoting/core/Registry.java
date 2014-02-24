package com.github.jremoting.core;

import java.util.List;

public interface Registry {
	void start();
	void close();
	List<ServiceProvider> getProviders(Invoke invoke);
	void publish(ServiceProvider provider);
	void unpublish(ServiceProvider provider);
	void subscribe(ServiceConsumer consumer);
	void unsubscribe(ServiceConsumer consumer);
	String getAppConfig(String appName, String key);
	String getServiceConfig(String serviceName, String key);
	
	void addListener(RegistryListener listener);
}
