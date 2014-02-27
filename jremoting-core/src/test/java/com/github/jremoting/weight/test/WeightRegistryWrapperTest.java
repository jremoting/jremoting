package com.github.jremoting.weight.test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alibaba.fastjson.JSON;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.RegistryEvent;
import com.github.jremoting.core.RegistryEvent.EventType;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.weight.WeightRegistryWrapper;



@RunWith(EasyMockRunner.class)
public class WeightRegistryWrapperTest {
	
	WeightRegistryWrapper wrapper;
	
	List<ServiceProvider> providers;
	
	Invoke invoke;
	
	String weightRuleJson;
	
	String interfaceName = "com.test.HelloService";
	String version = "1.0";
	String group = "test";
	String serviceName = interfaceName + ":" +version;
	String configFileName = "weight.rule";
	
	@Before
	public void setUp() {
		providers = new ArrayList<ServiceProvider>();
		for (int i = 0; i < 2; i++) {
			ServiceProvider provider = new ServiceProvider(interfaceName, version, group);
			provider.setAddress("10.10.1." + i + ":8686");
			providers.add(provider);
		}
		for (int i = 0; i < 2; i++) {
			ServiceProvider provider = new ServiceProvider(interfaceName, version, group);
			provider.setAddress("10.10.2." + i + ":8686");
			providers.add(provider);
		}
		invoke = new Invoke(interfaceName, version, group, "hello", null, new Object[0], new Class<?>[0]);
	}
	
	@Test
	public void testEmptyWeightRule() {
	
		Registry registry = createMockRegistry("");

		wrapper = new WeightRegistryWrapper(registry);

		List<ServiceProvider> outputProviders = wrapper.getProviders(invoke);

		assertArrayEquals(toAddressArray(providers),
				toAddressArray(outputProviders));
	}
	
	@Test
	public void testWrongFormatRule() {
		Registry registry = createMockRegistry("dsafasfdsafd#!#@!$!@$#@!");

		wrapper = new WeightRegistryWrapper(registry);

		List<ServiceProvider> outputProviders = wrapper.getProviders(invoke);

		assertArrayEquals(toAddressArray(providers),
				toAddressArray(outputProviders));
	}

	private Registry createMockRegistry(String jsonRule) {
		Registry registry = createMock(Registry.class);
		registry.addListener(anyObject(WeightRegistryWrapper.class));
		expect(registry.getProviders(invoke)).andReturn(providers);

		expect(registry.getServiceConfig(serviceName, configFileName)).andReturn(jsonRule);

		replay(registry);
		return registry;
	}
	
	@Test
	public void testNullWeightRuleAndRuleCache() {
		
		Registry registry = createMock(Registry.class);
		registry.addListener(anyObject(WeightRegistryWrapper.class));
		expect(registry.getProviders(invoke)).andReturn(providers);
		expect(registry.getServiceConfig(serviceName, configFileName)).andReturn(null);
		expect(registry.getProviders(invoke)).andReturn(providers);
		

		replay(registry);
		wrapper = new WeightRegistryWrapper(registry);

		List<ServiceProvider> outputProviders = wrapper.getProviders(invoke);

		assertArrayEquals(toAddressArray(providers),
				toAddressArray(outputProviders));
		
		outputProviders = wrapper.getProviders(invoke);

		assertArrayEquals(toAddressArray(providers),toAddressArray(outputProviders));

	}
	
	@Test
	public void testNormalWeightRule() {
		String json = "{ruleItems:[{ipPatterns:['10.10.1.*'],weight:2}]}";
		Registry registry = createMockRegistry(json);
		wrapper = new WeightRegistryWrapper(registry);

		List<ServiceProvider> outputProviders = wrapper.getProviders(invoke);
		
		String[] expectedAddresses = new String[]{"10.10.1.0:8686","10.10.1.0:8686","10.10.1.1:8686","10.10.1.1:8686","10.10.2.0:8686","10.10.2.1:8686"};
		
		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
		
		json = "{ruleItems:[{ipPatterns:['10.10.2.1', '10.10.1.*'],weight:2}]}";
		registry = createMockRegistry(json);
		wrapper = new WeightRegistryWrapper(registry);
		outputProviders = wrapper.getProviders(invoke);
		
		System.out.println(JSON.toJSONString(toAddressArray(outputProviders)));
		expectedAddresses = new String[]{"10.10.1.0:8686","10.10.1.0:8686","10.10.1.1:8686","10.10.1.1:8686","10.10.2.0:8686","10.10.2.1:8686","10.10.2.1:8686"};
		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
		
		json = "{ruleItems:[{ipPatterns:['10.10.2.1'],weight:2},{ipPatterns:['10.10.1.*'],weight:2}]}";
		registry = createMockRegistry(json);
		wrapper = new WeightRegistryWrapper(registry);
		outputProviders = wrapper.getProviders(invoke);
		
		System.out.println(JSON.toJSONString(toAddressArray(outputProviders)));
		expectedAddresses = new String[]{"10.10.1.0:8686","10.10.1.0:8686","10.10.1.1:8686","10.10.1.1:8686","10.10.2.0:8686","10.10.2.1:8686","10.10.2.1:8686"};
		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
	}
	
	@Test
	public void testWeightRuleChange() {
		Registry registry = createMock(Registry.class);
		registry.addListener(anyObject(WeightRegistryWrapper.class));
		expect(registry.getProviders(invoke)).andReturn(providers);
		expect(registry.getServiceConfig(serviceName, configFileName)).andReturn(null);
		expect(registry.getProviders(invoke)).andReturn(providers);
		

		replay(registry);
		wrapper = new WeightRegistryWrapper(registry);
		
		List<ServiceProvider> outputProviders = wrapper.getProviders(invoke);

		assertArrayEquals(toAddressArray(providers),
				toAddressArray(outputProviders));
		
		
		RegistryEvent event = new RegistryEvent();
		event.setServiceName(serviceName);
		event.setFileName(configFileName);
		event.setNewContent("{ruleItems:[{ipPatterns:['10.10.1.*'],weight:2}]}");
		event.setType(EventType.SERVICE_CONFIG_CHANGED);
		
		wrapper.onEvent(event);
		
		outputProviders = wrapper.getProviders(invoke);
		 
		String[] expectedAddresses = new String[]{"10.10.1.0:8686","10.10.1.0:8686","10.10.1.1:8686","10.10.1.1:8686","10.10.2.0:8686","10.10.2.1:8686"};
		
		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
	}
	
	public String[] toAddressArray(List<ServiceProvider> providers) {
		String[] array = new String[providers.size()];
		for (int i = 0; i < providers.size(); i++) {
			array[i] = providers.get(i).getAddress();
		}
		return array;
	}
}
