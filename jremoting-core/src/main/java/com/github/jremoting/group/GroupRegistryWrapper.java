package com.github.jremoting.group;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.github.jremoting.core.AbstractRegistryWrapper;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;
import com.github.jremoting.core.RegistryEvent.EventType;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.util.NetUtil;

public class GroupRegistryWrapper extends AbstractRegistryWrapper {
	
	private String fileName = "group.rule";
	private final String localIp = NetUtil.getLocalIp();
	

	protected Set<ServiceProvider> publishedProviders = new CopyOnWriteArraySet<ServiceProvider>();
	protected Set<ServiceConsumer> subcribedConsumers = new CopyOnWriteArraySet<ServiceConsumer>();

	
	public GroupRegistryWrapper(Registry wrappedRegistry) {
		super(wrappedRegistry);
	}

	@Override
	public void publish(ServiceProvider provider) {
		String appConfig =  this.wrappedRegistry.getAppConfig(provider.getAppName(), fileName); 
		String serviceConfig = this.wrappedRegistry.getServiceConfig(provider.getServiceName(), fileName);
		
		
		GroupStrategy strategy = new GroupStrategy(provider.getAppName(),appConfig
				,provider.getServiceName() ,serviceConfig, localIp);
		
		String newGroup = strategy.getNewGroup(provider);
		if(!newGroup.equals(provider.getGroup())) {
			provider.setGroup(newGroup);
		}
		
		this.wrappedRegistry.publish(provider);
		publishedProviders.add(provider);
	}

	@Override
	public void subscribe(ServiceConsumer consumer) {
		String appConfig = this.wrappedRegistry.getAppConfig(consumer.getAppName(), fileName); 
		String serviceConfig = this.wrappedRegistry.getServiceConfig(consumer.getServiceName(), fileName);
	
		
		GroupStrategy strategy = new GroupStrategy(consumer.getAppName(),appConfig
				,consumer.getServiceName() ,serviceConfig, localIp);
		
		String newGroup = strategy.getNewGroup(consumer);
		if(!newGroup.equals(consumer.getGroup())) {
			consumer.setGroup(newGroup);
		}
	
		this.wrappedRegistry.subscribe(consumer);
		subcribedConsumers.add(consumer);
	}
	
	@Override
	public void unpublish(ServiceProvider provider) {
		this.wrappedRegistry.unpublish(provider);
		publishedProviders.remove(provider);
	}

	@Override
	public void unsubscribe(ServiceConsumer consumer) {
		this.wrappedRegistry.unsubscribe(consumer);
		subcribedConsumers.remove(consumer);
	}
	
	@Override
	public void onEvent(RegistryEvent event) {
		if(event.getType() == EventType.APP_CONFIG_CHANGED && this.fileName.equals(event.getFileName()) ) {
			republishIfGroupChanged();
			resubscribeIfGroupChanged();
		}
		else if(event.getType() == EventType.SERVICE_CONFIG_CHANGED && this.fileName.equals(event.getFileName())){
			republishIfGroupChanged();
			resubscribeIfGroupChanged();
		}
	}
	
	private void republishIfGroupChanged() {
		for (ServiceProvider provider : publishedProviders) {
			String appConfig = this.wrappedRegistry.getAppConfig(provider.getAppName(), fileName);
			String serviceConfig = this.wrappedRegistry.getServiceConfig(provider.getServiceName(), fileName);
			
			GroupStrategy strategy = new GroupStrategy(provider.getAppName(),appConfig
					,provider.getServiceName() ,serviceConfig, localIp);
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
			String appConfig = this.wrappedRegistry.getAppConfig(consumer.getAppName(), fileName);
			String serviceConfig = this.wrappedRegistry.getServiceConfig(consumer.getServiceName(), fileName);
			
			GroupStrategy strategy = new GroupStrategy(consumer.getAppName(),appConfig
					,consumer.getServiceName() ,serviceConfig, localIp);
			String newGroup = strategy.getNewGroup(consumer);
			if(!newGroup.equals(consumer.getGroup())) {
				this.wrappedRegistry.unsubscribe(consumer);
				consumer.setGroup(newGroup);
				this.wrappedRegistry.subscribe(consumer);
			}
		}
	}


}
