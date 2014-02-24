package com.github.jremoting.registry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.jremoting.core.Registry;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.group.GroupStrategy;

public class GroupRegistryWrapper extends AbstractRegistryWrapper {
	
	String appName;
	String groupRuleKey = "grouping.rule";
	String localIp = "";
	
	List<ServiceProvider> publishedProviders = new CopyOnWriteArrayList<ServiceProvider>();
	List<ServiceConsumer> subcribedConsumers = new CopyOnWriteArrayList<ServiceConsumer>();
	
	public GroupRegistryWrapper(Registry originalRegistry) {
		super(originalRegistry);
	}

	@Override
	public void publish(ServiceProvider provider) {
		String appConfig = this.wrappedRegistry.getAppConfig(appName, groupRuleKey); 
		String serviceConfig = this.wrappedRegistry.getServiceConfig(provider.getServiceName(), groupRuleKey);
		
		
		GroupStrategy strategy = new GroupStrategy(appConfig, serviceConfig, localIp);
		
		String newGroup = strategy.getNewGroup(provider);
		if(newGroup.equals(provider.getGroup())) {
			this.wrappedRegistry.publish(provider);
		}
		else {
			provider.setGroup(newGroup);
			this.wrappedRegistry.publish(provider);
		}
		
		publishedProviders.add(provider);
		
	}
	
	@Override
	public void subscribe(ServiceConsumer consumer) {
		String appConfig = this.wrappedRegistry.getAppConfig(appName, groupRuleKey); 
		String serviceConfig = this.wrappedRegistry.getServiceConfig(consumer.getServiceName(), groupRuleKey);
	
		
		GroupStrategy strategy = new GroupStrategy(appConfig, serviceConfig, localIp);
		
		String newGroup = strategy.getNewGroup(consumer);
		if(newGroup.equals(consumer.getGroup())) {
			this.wrappedRegistry.subscribe(consumer);
		}
		else {
			consumer.setGroup(newGroup);
			this.wrappedRegistry.subscribe(consumer);
		}
		
		subcribedConsumers.add(consumer);
	}
	
	@Override
	public void onAppConfigChanged(String appName, String key, String newValue) {
		if(!groupRuleKey.equals(key)) {
			return;
		}
	
		republishIfGroupChanged();
		resubscribeIfGroupChanged();
	}
	
	@Override
	public void onServiceConfigChanged(String serviceName, String key, String newValue) {
		if(!groupRuleKey.equals(key)) {
			return;
		}

		republishIfGroupChanged();
		resubscribeIfGroupChanged();
	}
	
	private void republishIfGroupChanged() {
		for (ServiceProvider provider : publishedProviders) {
			String appGroupConfig = this.wrappedRegistry.getAppConfig(appName, groupRuleKey);
			String serviceGroupConfig = this.wrappedRegistry.getServiceConfig(provider.getServiceName(), groupRuleKey);
			
			GroupStrategy strategy = new GroupStrategy(appGroupConfig, serviceGroupConfig, localIp);
			String newGroup = strategy.getNewGroup(provider);
			if(!newGroup.equals(provider.getGroup())) {
				this.wrappedRegistry.unpublish(provider);
				provider.setGroup(newGroup);
				this.wrappedRegistry.publish(provider);
			}
		}
	}
	
	private void resubscribeIfGroupChanged(){
		for (ServiceConsumer consumer : subcribedConsumers) {
			String appGroupConfig = this.wrappedRegistry.getAppConfig(appName, groupRuleKey);
			String serviceGroupConfig = this.wrappedRegistry.getServiceConfig(consumer.getServiceName(), groupRuleKey);
			
			GroupStrategy strategy = new GroupStrategy(appGroupConfig, serviceGroupConfig, localIp);
			String newGroup = strategy.getNewGroup(consumer);
			if(!newGroup.equals(consumer.getGroup())) {
				this.wrappedRegistry.unsubscribe(consumer);
				consumer.setGroup(newGroup);
				this.wrappedRegistry.subscribe(consumer);
			}
		}
	}


}
