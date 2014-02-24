package com.github.jremoting.core;

import java.util.List;

public interface RegistryListener {
	void onProvidersChanged(String serviceId, List<ServiceProvider> newProviders);
	void onGlobalConfigChanged(String key, String newValue);
	void onAppConfigChanged(String appName, String key, String newValue);
	void onServiceConfigChanged(String serviceName,String key, String newValue);
	void onRecover();
}
