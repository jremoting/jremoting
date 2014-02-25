package com.github.jremoting.route;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.AbstractRegistryWrapper;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.core.RegistryEvent.EventType;

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
	public void onEvent(RegistryEvent event) {
		if(event.getType() == EventType.SERVICE_CONFIG_CHANGED && configFileName.equals(event.getFileName())) {
			RouteRule routeRule = routeRuleParser.parse(event.getNewContent());
			
			for (String serviceId : cachedRouteStrategies.keySet()) {
				if(serviceId.contains(event.getServiceName())) {
					RouteStrategy oldStrategy = cachedRouteStrategies.get(event.getServiceName());
					List<ServiceProvider> allProviders = oldStrategy.getAllProviders();

					RouteStrategy newStrategy = new RouteStrategy(allProviders, routeRule);
					cachedRouteStrategies.put(serviceId, newStrategy);
				}
			}
		}
		
		if(event.getType() == EventType.PROVIDERS_CHANGED) {
			RouteStrategy oldStrategy = cachedRouteStrategies.get(event.getServiceId());
			RouteRule routeRule = oldStrategy.getRouteRule();

			RouteStrategy newStrategy = new RouteStrategy(event.getNewProviders(), routeRule);

			cachedRouteStrategies.put(event.getServiceId(), newStrategy);
		}
	}

}
