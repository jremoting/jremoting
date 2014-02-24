package com.github.jremoting.core;

import java.util.List;

public interface RegistryListener {
	void onProvidersChanged(String serviceName, List<ServiceProvider> newProviders);
	void onAppConfigChanged(String appName, String key, String newValue);
	void onServiceConfigChanged(String serviceName,String key, String newValue);
}
