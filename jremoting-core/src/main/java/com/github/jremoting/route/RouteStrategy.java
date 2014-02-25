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
	private  MethodRouteRule methodRouteRule;
	private  ParameterRouteRule parameterRouteRule;
	private boolean isEmpty = true;
	private final RouteRule routeRule;
	
	public RouteStrategy(List<ServiceProvider> allProviders, RouteRule routeRule) {
		this.allProviders = allProviders;
		this.routeRule = routeRule;
		
		if(allProviders == null || allProviders.size() == 0 || routeRule == null) {
			isEmpty = true;
		}
		else {
			if(routeRule instanceof MethodRouteRule) {
				this.methodRouteRule = (MethodRouteRule)routeRule;
			}
			else {
				this.methodRouteRule = null;
			}
			if(routeRule instanceof ParameterRouteRule) {
				this.parameterRouteRule = (ParameterRouteRule)routeRule;
			}
			else {
				this.parameterRouteRule = null;
			}
			
			initTableNameToProvidersCache(routeRule.createRouteTables());
		}	
	}
	
	public List<ServiceProvider> getAllProviders() {
		return this.allProviders;
	}
	
	public List<ServiceProvider> getProviders(Invoke invoke) {
		if(this.isEmpty || this.tableNameToProviderMap == null || this.tableNameToProviderMap.size() == 0) {
			return allProviders;
		}
		
		if(this.parameterRouteRule != null) {
			String tableName = this.parameterRouteRule.selectRouteTable(invoke.getMethodName(), 
					invoke.getParameterTypeNames(), invoke.getArgs());
			if(tableName != null) {
				List<ServiceProvider> parameterTargetProviders = this.tableNameToProviderMap.get(tableName);
				if(parameterTargetProviders != null && parameterTargetProviders.size() > 0) {
					return parameterTargetProviders;
				}
			}
		}
		
		if(this.methodRouteRule != null) {
			String tableName = this.methodRouteRule.selectRouteTable(invoke.getMethodName(), invoke.getParameterTypeNames());
			if(tableName != null) {
				List<ServiceProvider> methodTargetProviders = this.tableNameToProviderMap.get(tableName);
				if(methodTargetProviders != null && methodTargetProviders.size() > 0) {
					return methodTargetProviders;
				}
			}
		}
		
		List<ServiceProvider> serviceTargetProviders = this.tableNameToProviderMap.get(0);
		
		if(serviceTargetProviders != null && serviceTargetProviders.size() > 0) {
			return serviceTargetProviders;
		}

		return allProviders;
	}
	
	private void initTableNameToProvidersCache(Map<String, String[]> routeTables) {
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
