package com.github.jremoting.core.test;

import com.github.jremoting.core.AbstractRegistryWrapper;
import com.github.jremoting.core.Registry;

public class TestRegistryWrapper extends AbstractRegistryWrapper {

	private String configFileName = "test.rule";
	
	public TestRegistryWrapper(Registry wrappedRegistry) {
		super(wrappedRegistry);
		
	}
	
	@Override
	public void start() {
		super.start();
		
		super.getGlobalConfig(configFileName);
		super.getAppConfig("test", configFileName);
		super.getServiceConfig("testService:1.0", configFileName);
		
	}
	
	
	@Override
	public void onAppConfigChanged(String appName, String fileName, String newContent) {
		System.out.println(String.format("appName:%s, fileName:%s,newContent:%s", appName,fileName, newContent));
	}

	@Override
	public void onServiceConfigChanged(String serviceName,String fileName, String newContent) {
		System.out.println(String.format("serviceName:%s, fileName:%s,newContent:%s", serviceName,fileName, newContent));

	}
	@Override
	public void onGlobalConfigChanged(String fileName, String newContent) {
		System.out.println(String.format("fileName:%s,newContent:%s",fileName, newContent));

	}

}
