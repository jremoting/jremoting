package com.github.jremoting.core;

import java.util.List;

public interface RegistryListener { 
	void onProvidersChanged(String serviceId, List<ServiceProvider> newProviders);
	void onGlobalConfigChanged(String fileName, String newContent);
	void onAppConfigChanged(String appName, String fileName, String newContent);
	void onServiceConfigChanged(String serviceName,String fileName, String newContent);
	void onRecover();
}
