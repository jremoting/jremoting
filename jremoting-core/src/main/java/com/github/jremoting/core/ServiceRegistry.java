package com.github.jremoting.core;

import java.util.List;

public interface ServiceRegistry {
	List<ServiceProviderInfo> lookup(String serviceName);
	void subscibe(String serviceName);
	void publish(ServiceProviderInfo providerInfo);
}
