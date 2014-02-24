package com.github.jremoting.group;

import com.github.jremoting.core.AbstractRegistryWrapper;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;

public class GroupRegistryWrapper extends AbstractRegistryWrapper {
	
	String appName;
	String fileName = "group.rule";
	String localIp = "";
	

	
	public GroupRegistryWrapper(Registry originalRegistry) {
		super(originalRegistry);
	}

	@Override
	public void publish(ServiceProvider provider) {
		String appConfig =  super.getAppConfig(appName, fileName); 
		String serviceConfig = super.getServiceConfig(provider.getServiceName(), fileName);
		
		
		GroupStrategy strategy = new GroupStrategy(appConfig, serviceConfig, localIp);
		
		String newGroup = strategy.getNewGroup(provider);
		if(newGroup.equals(provider.getGroup())) {
			super.publish(provider);
		}
		else {
			provider.setGroup(newGroup);
			super.publish(provider);
		}
	}

	@Override
	public void subscribe(ServiceConsumer consumer) {
		String appConfig = super.getAppConfig(appName, fileName); 
		String serviceConfig = super.getServiceConfig(consumer.getServiceName(), fileName);
	
		
		GroupStrategy strategy = new GroupStrategy(appConfig, serviceConfig, localIp);
		
		String newGroup = strategy.getNewGroup(consumer);
		if(newGroup.equals(consumer.getGroup())) {
			super.subscribe(consumer);
		}
		else {
			consumer.setGroup(newGroup);
			super.subscribe(consumer);
		}
	}
	
	@Override
	public void onRecover() {
		for (ServiceProvider provider : publishedProviders) {
			String appGroupConfig =  super.getAppConfig(appName, fileName);
			String serviceGroupConfig = super.getServiceConfig(provider.getServiceName(), fileName);
			
			GroupStrategy strategy = new GroupStrategy(appGroupConfig, serviceGroupConfig, localIp);
			String newGroup = strategy.getNewGroup(provider);
			if(!newGroup.equals(provider.getGroup())) {
				provider.setGroup(newGroup);
			}
		}
		
		for (ServiceConsumer consumer : subcribedConsumers) {
			String appGroupConfig = super.getAppConfig(appName, fileName);
			String serviceGroupConfig = super.getServiceConfig(consumer.getServiceName(), fileName);
			
			GroupStrategy strategy = new GroupStrategy(appGroupConfig, serviceGroupConfig, localIp);
			String newGroup = strategy.getNewGroup(consumer);
			if(!newGroup.equals(consumer.getGroup())) {
				consumer.setGroup(newGroup);
			}
		}
	}
	
	@Override
	public void onAppConfigChanged(String appName, String fileName, String newContent) {
		if(!fileName.equals(fileName)) {
			return;
		}
	
		republishIfGroupChanged();
		resubscribeIfGroupChanged();
	}
	
	@Override
	public void onServiceConfigChanged(String serviceName, String fileName, String newContent) {
		if(!fileName.equals(fileName)) {
			return;
		}

		republishIfGroupChanged();
		resubscribeIfGroupChanged();
	}
	
	private void republishIfGroupChanged() {
		for (ServiceProvider provider : publishedProviders) {
			String appGroupConfig = super.getAppConfig(appName, fileName);
			String serviceGroupConfig = super.getServiceConfig(provider.getServiceName(), fileName);
			
			GroupStrategy strategy = new GroupStrategy(appGroupConfig, serviceGroupConfig, localIp);
			String newGroup = strategy.getNewGroup(provider);
			if(!newGroup.equals(provider.getGroup())) {
				super.unpublish(provider);
				provider.setGroup(newGroup);
				super.publish(provider);
			}
		}
	}
	
	private void resubscribeIfGroupChanged(){
		for (ServiceConsumer consumer : subcribedConsumers) {
			String appGroupConfig = super.getAppConfig(appName, fileName);
			String serviceGroupConfig = super.getServiceConfig(consumer.getServiceName(), fileName);
			
			GroupStrategy strategy = new GroupStrategy(appGroupConfig, serviceGroupConfig, localIp);
			String newGroup = strategy.getNewGroup(consumer);
			if(!newGroup.equals(consumer.getGroup())) {
				super.unsubscribe(consumer);
				consumer.setGroup(newGroup);
				super.subscribe(consumer);
			}
		}
	}


}
