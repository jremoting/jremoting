package com.github.jremoting.core.test;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.AbstractRegistryWrapper;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;

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
	public void onEvent(RegistryEvent event) {
		System.out.println(JSON.toJSONString(event));
	}


}
