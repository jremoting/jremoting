package com.github.jremoting.registry;

import java.util.List;

import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.group.GroupRule;

public interface RemoteRegistry {
	void start();
	void close();
	void publish(ServiceProvider provider);
	void subscribe(ServiceConsumer consumer);
	void unpublish(ServiceProvider provider);
	void unsubscribe(ServiceConsumer consumer);
	void subscribeConfig(String serviceName);
	void subscribeRouteRules(String appName, String serviceName);
	List<GroupRule> getGroupRules(String appName, String serviceName);
	void addListener(RemoteRegistryListener listener);
}
