package com.github.jremoting.weight;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.AbstractRegistryWrapper;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;
import com.github.jremoting.core.RegistryEvent.EventType;
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
	public void onEvent(RegistryEvent event) {
		if(event.getType() != EventType.SERVICE_CONFIG_CHANGED) {
			return;
		}
		if(!configFileName.equals(event.getFileName())) {
			return;
		}
		
		WeightStrategy strategy = new WeightStrategy(event.getNewContent());
		
		cachedWeightStrategies.put(event.getServiceName(), strategy);

	}

}
