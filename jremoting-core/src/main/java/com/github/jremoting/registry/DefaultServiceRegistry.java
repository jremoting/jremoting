package com.github.jremoting.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jremoting.core.ServiceProviderInfo;
import com.github.jremoting.core.ServiceRegistry;

public class DefaultServiceRegistry implements ServiceRegistry {

	private volatile Map<String, List<ServiceProviderInfo>> providerInfos = new HashMap<String, List<ServiceProviderInfo>>();
	
	private List<String> subscribedServices = new ArrayList<String>();
	
	@Override
	public List<ServiceProviderInfo> getProviderInfos(String serviceName) {
		return providerInfos.get(serviceName);
	}
	
	public void subscribe(String serviceName) {
		this.subscribedServices.add(serviceName);
	}
}
