package com.github.jremoting.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jremoting.core.ServiceProviderInfo;
import com.github.jremoting.core.ServiceRegistry;

public class DefaultServiceRegistry implements ServiceRegistry {

	private volatile Map<String, List<ServiceProviderInfo>> providerInfos = new HashMap<String, List<ServiceProviderInfo>>();
	

	@Override
	public List<ServiceProviderInfo> lookup(String serviceName) {
		return providerInfos.get(serviceName);
	}


	@Override
	public void subscibe(String serviceName) {
	}

	@Override
	public void publish(ServiceProviderInfo providerInfo) {
	}
}
