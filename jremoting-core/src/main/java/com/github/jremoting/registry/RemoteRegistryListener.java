package com.github.jremoting.registry;

import java.util.List;

import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.group.GroupRule;
import com.github.jremoting.route.RouteRule;

public interface RemoteRegistryListener {
	 void onProviderChanged(String serviceName, List<ServiceProvider> newProviders);
	 void onRecover();
	 void onRouteRuleChanged(String serviceName, List<RouteRule> newRouteRules);
	 void onConfigChanged(String serviceName,String key, String value);
	 void onGroupRuleChanged(String serviceName, List<GroupRule> newGroupRules);
}
