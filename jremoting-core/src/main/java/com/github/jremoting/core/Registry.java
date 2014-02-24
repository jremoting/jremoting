package com.github.jremoting.core;

import java.util.List;
import java.util.Map;

public interface Registry {
	void start();
	void close();
	
	List<ServiceProvider> getProviders(Invoke invoke);
	
	Map<String, ServiceProvider> getLocalProviders();
	
	String getGlobalConfig(String fileName);
	String getAppConfig(String appName, String fileName);
	String getServiceConfig(String serviceName, String fileName);
	
	void publish(ServiceProvider provider);
	void unpublish(ServiceProvider provider);
	void subscribe(ServiceConsumer consumer);
	void unsubscribe(ServiceConsumer consumer); 

	void addListener(RegistryListener listener);
}
