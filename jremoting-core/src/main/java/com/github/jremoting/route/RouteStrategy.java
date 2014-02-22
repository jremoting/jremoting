package com.github.jremoting.route;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.ServiceProvider;

public class RouteStrategy {
	
	private final List<ServiceProvider> allProviders;
	private final List<RouteRule> routeRules;
	
	private List<ServiceProvider> cachedInterfaceLevelProviders= Collections.emptyList();
	private Map<String, List<ServiceProvider>> cachedMethodLevelProviders = new HashMap<String, List<ServiceProvider>>();
	private Map<String, List<ServiceProvider>> cachedArgumentLevelProviders = new HashMap<String, List<ServiceProvider>>();
	
	public RouteStrategy(List<ServiceProvider> allProviders, List<RouteRule> routeRules) {
		this.allProviders = allProviders;
		this.routeRules = routeRules;
		this.warmupCache();
	}
	
	public List<ServiceProvider> getProviders(Invoke invoke) {
		return null;
	}
	
	private void warmupCache() {
		
	}
}
