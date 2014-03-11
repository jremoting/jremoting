package com.github.jremoting.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.util.WildcardUtil;

public class RouteStrategy {
	
	private  final List<ServiceProvider> allProviders;
	
	private  Map<String, List<ServiceProvider>> tableNameToProviderMap;
	private boolean isEmpty = false;
	private final RouteRule routeRule;
	
	public RouteStrategy(List<ServiceProvider> allProviders, RouteRule routeRule) {
		this.allProviders = allProviders;
		this.routeRule = routeRule;
		
		if(allProviders == null || allProviders.size() == 0 || routeRule == null) {
			isEmpty = true;
		}
		else {
	
			initTableNameToProvidersCache(routeRule.defineRouteTables());
		}	
	}
	
	public List<ServiceProvider> getAllProviders() {
		return this.allProviders;
	}
	
	public List<ServiceProvider> getProviders(Invoke invoke) {
		if(this.isEmpty || this.tableNameToProviderMap == null || this.tableNameToProviderMap.size() == 0) {
			return allProviders;
		}
		
		if(this.routeRule != null) {
			String tableName = this.routeRule.selectRouteTable(invoke.getMethodName(), 
					invoke.getParameterTypeNames(), invoke.getArgs());
			if(tableName != null) {
				List<ServiceProvider> parameterTargetProviders = this.tableNameToProviderMap.get(tableName);
				if(parameterTargetProviders != null && parameterTargetProviders.size() > 0) {
					return parameterTargetProviders;
				}
			}
		}
	
		return allProviders;
	}
	
	private void initTableNameToProvidersCache(Map<String,String[]> routeTables) {
		if(routeTables == null || routeTables.isEmpty()) {
			this.tableNameToProviderMap = null;
			return;
		}
		this.tableNameToProviderMap = new HashMap<String, List<ServiceProvider>>();
		
		for (String tableName : routeTables.keySet()) {
			 String[] patterns = routeTables.get(tableName);
			if(patterns != null && patterns.length > 0) {
				List<ServiceProvider> matchedProviders = new ArrayList<ServiceProvider>();
				for (ServiceProvider provider: allProviders) {
					for (String  pattern : patterns) {
						if(WildcardUtil.equalsOrMatch(provider.getIp(), pattern)) {
							matchedProviders.add(provider);
						}
					}
				}
				
				if(matchedProviders.size() > 0) {
					this.tableNameToProviderMap.put(tableName, matchedProviders);
				}
			}
		}
	}

	public RouteRule getRouteRule() {
		return routeRule;
	}
}
