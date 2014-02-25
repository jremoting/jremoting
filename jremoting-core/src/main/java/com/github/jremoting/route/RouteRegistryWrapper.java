package com.github.jremoting.route;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.AbstractRegistryWrapper;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.ServiceProvider;

public class RouteRegistryWrapper extends AbstractRegistryWrapper {

	private final RouteRuleParser routeRuleParser;
	private final String configFileName = "route.rule";
	private ConcurrentHashMap<String, RouteStrategy> cachedRouteStrategies = new ConcurrentHashMap<String, RouteStrategy>();
	
	public RouteRegistryWrapper(RouteRuleParser routeRuleParser,Registry wrappedRegistry) {
		super(wrappedRegistry);
		this.routeRuleParser = routeRuleParser;
	}
	
	public RouteRegistryWrapper(Registry wrappedRegistry) {
		super(wrappedRegistry);
		this.routeRuleParser = new JavaRouteRuleParser();
	}
	
	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {
		RouteStrategy routeStrategy = cachedRouteStrategies.get(invoke.getServiceId());
		if(routeStrategy == null) {
			
			 String config = this.wrappedRegistry.getServiceConfig(invoke.getServiceName(), configFileName);
			 List<ServiceProvider> allProviders = this.wrappedRegistry.getProviders(invoke);
			 
			 RouteRule routeRule = routeRuleParser.parse(config);
			 
			 routeStrategy  = new RouteStrategy(allProviders, routeRule);
			 
			 cachedRouteStrategies.put(invoke.getServiceId(), routeStrategy); 
		}
		return routeStrategy.getProviders(invoke);
	}
	
	@Override
	public void onServiceConfigChanged(String serviceName, String fileName,
			String newContent) {
		if (!configFileName.equals(fileName)) {
			return;
		}
		
		RouteRule routeRule = routeRuleParser.parse(newContent);
		
		for (String serviceId : cachedRouteStrategies.keySet()) {
			if(serviceId.contains(serviceName)) {
				RouteStrategy oldStrategy = cachedRouteStrategies.get(serviceName);
				List<ServiceProvider> allProviders = oldStrategy.getAllProviders();

				RouteStrategy newStrategy = new RouteStrategy(allProviders, routeRule);
				cachedRouteStrategies.put(serviceId, newStrategy);
			}
		}
		
	}
	
	@Override
	public void onProvidersChanged(String serviceId, List<ServiceProvider> newProviders) {
		RouteStrategy oldStrategy = cachedRouteStrategies.get(serviceId);
		RouteRule routeRule = oldStrategy.getRouteRule();

		RouteStrategy newStrategy = new RouteStrategy(newProviders, routeRule);

		cachedRouteStrategies.put(serviceId, newStrategy);
	}
	
}
