package com.github.jremoting.weight;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.AbstractRegistryWrapper;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.ServiceProvider;

public class WeightRegistryWrapper extends AbstractRegistryWrapper {
	
	private String configFileName = "weight.rule";
	
	private ConcurrentHashMap<String, WeightStrategy> cachedWeightStrategies = new ConcurrentHashMap<String, WeightStrategy>();

	public WeightRegistryWrapper(Registry wrappedRegistry) {
		super(wrappedRegistry);
	}
	
	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {
		
		List<ServiceProvider> originalProviders = this.wrappedRegistry.getProviders(invoke);
		
		WeightStrategy strategy = cachedWeightStrategies.get(invoke.getServiceName());
		if(strategy == null) {
			String rule = this.wrappedRegistry.getServiceConfig(invoke.getServiceName(), configFileName);
			strategy = new WeightStrategy(rule);
			cachedWeightStrategies.put(invoke.getServiceName(), strategy);
		}
		
		return strategy.applyWeightRule(originalProviders);
	}
	
	@Override
	public void onServiceConfigChanged(String serviceName,String fileName, String newContent) {
		if(!configFileName.equals(fileName)) {
			return;
		}
		
		WeightStrategy strategy = new WeightStrategy(newContent);
		
		cachedWeightStrategies.put(serviceName, strategy);

	}

}
