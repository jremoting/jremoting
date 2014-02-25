package com.github.jremoting.registry;

import java.util.List;
import java.util.Map;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryListener;
import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.core.ServiceProvider;

public class DubboRegistry implements Registry {

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ServiceProvider> getProviders(Invoke invoke) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ServiceProvider> getLocalProviders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGlobalConfig(String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAppConfig(String appName, String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServiceConfig(String serviceName, String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void publish(ServiceProvider provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpublish(ServiceProvider provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribe(ServiceConsumer consumer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unsubscribe(ServiceConsumer consumer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(RegistryListener listener) {
		// TODO Auto-generated method stub
		
	}

}
