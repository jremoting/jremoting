package com.github.jremoting.route.test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.route.RouteRegistryWrapper;

@RunWith(EasyMockRunner.class)
public class RouteRegistryWrapperTest {
	RouteRegistryWrapper wrapper;
	
	List<ServiceProvider> providers;
	
	String interfaceName = "com.test.HelloService";
	String version = "1.0";
	String group = "test";
	String serviceName = interfaceName + ":" +version;
	String configFileName = "route.rule";
	
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
	}
	
	@Test
	public void testEmptyRule() {
		Registry registry = createMockRegistry("");
		wrapper = new RouteRegistryWrapper(registry);
		
		List<ServiceProvider> outputProviders = wrapper.getProviders(new Invoke(interfaceName, version, group,
				"hello", null, new Object[0], new Class<?>[0]));
		
		assertArrayEquals(toAddressArray(providers),
				toAddressArray(outputProviders));
	}
	@Test
	public void testNullRule() {
		Registry registry = createMockRegistry(null);
		wrapper = new RouteRegistryWrapper(registry);
		
		List<ServiceProvider> outputProviders = wrapper.getProviders(new Invoke(interfaceName, version, group,
				"hello", null, new Object[0], new Class<?>[0]));
		
		assertArrayEquals(toAddressArray(providers),
				toAddressArray(outputProviders));
	}
	
	@Test
	public void testInvalidFormatRule() {
		Registry registry = createMockRegistry("#!R@DAFQ");
		wrapper = new RouteRegistryWrapper(registry);
		
		List<ServiceProvider> outputProviders = wrapper.getProviders(new Invoke(interfaceName, version, group,
				"hello", null, new Object[0], new Class<?>[0]));
		
		assertArrayEquals(toAddressArray(providers),
				toAddressArray(outputProviders));
	}
	
	@Test
	public void testServiceLevelRule() {
		StringBuilder builder = new StringBuilder();
		builder.append("{                                                                           ");
		builder.append("	defineRouteTables : function () {                                       ");
		builder.append("		return {                                                            ");
		builder.append("			all : ['10.10.2.*']                                           ");
		builder.append("		}                                                                   ");
		builder.append("	} ,                                                                     ");
		builder.append("	selectRouteTable: function(){return 'all';}                            ");
		builder.append("}                                                                           ");
		
		
		Registry registry = createMockRegistry(builder.toString());
		wrapper = new RouteRegistryWrapper(registry);

		List<ServiceProvider> outputProviders = wrapper.getProviders(new Invoke(interfaceName, version, group,
				"hello", null, new Object[0], new Class<?>[0]));
		
		String[] expectedAddresses = new String[]{"10.10.2.0:8686","10.10.2.1:8686"};
		
		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
		
		//System.out.println(JSON.toJSONString(toAddressArray(outputProviders)));
	}
	
	@Test
	public void testMethodLevelRule() {
		StringBuilder builder = new StringBuilder();
		builder.append("{                                                                           ");
		builder.append("	defineRouteTables : function () {                                       ");
		builder.append("		return {                                                            ");
		builder.append("			read : ['10.10.1.*', '10.10.2.0'],                              ");
		builder.append("			write : ['10.10.2.*']                                           ");
		builder.append("		}                                                                   ");
		builder.append("	},                                                                      ");
		builder.append("	selectRouteTable :function(methodName,parameterTypeNames) {             ");
		builder.append("		if(methodName.indexOf('get') == 0) {                                 ");
		builder.append("			return 'read';                                                  ");
		builder.append("		}                                                                   ");
		builder.append("		else if(methodName.indexOf('write') == 0) {                          ");
		builder.append("			return 'write'                                                  ");
		builder.append("		} else{return null;}                                                ");
		builder.append("	}                                                                      ");
		builder.append("}                                                                           ");
		
		Registry registry = createMockRegistry(builder.toString());
		wrapper = new RouteRegistryWrapper(registry);

		//return read
		List<ServiceProvider> outputProviders = wrapper.getProviders(new Invoke(interfaceName, version, group,
				"getXXX", null, new Object[0], new Class<?>[0]));
		
		String[] expectedAddresses = new String[]{"10.10.1.0:8686","10.10.1.1:8686","10.10.2.0:8686"};
		
		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
		
		//return write
		outputProviders = wrapper.getProviders(new Invoke(interfaceName, version, group,
				"write", null, new Object[0], new Class<?>[0]));
		
		expectedAddresses = new String[]{"10.10.2.0:8686","10.10.2.1:8686"};
		
		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
		
		//return null
		outputProviders = wrapper.getProviders(new Invoke(interfaceName, version, group,
				"sfdasadf", null, new Object[0], new Class<?>[0]));
				
		assertArrayEquals(toAddressArray(providers), toAddressArray(outputProviders));
	}
	
	@Test
	public void testParameterRule() {
		StringBuilder builder = new StringBuilder();
		builder.append("{                                                                           ");
		builder.append("	defineRouteTables : function () {                                       ");
		builder.append("		return {                                                            ");
		builder.append("			read : ['10.10.1.0', '10.10.2.0'],                              ");
		builder.append("			write : ['*']                                                   ");
		builder.append("		}                                                                   ");
		builder.append("	},                                                                      ");
		builder.append("	selectRouteTable :function(methodName,parameterTypeNames,args) {     ");
		builder.append("		if(methodName == 'hello' && args[0].name == 'aaa' && (args[0].son&&args[0].son.name=='bbb')) {             ");
		builder.append("			return 'write';                                                 ");
		builder.append("		}                                                                   ");
		builder.append("		else {                                                              ");
		builder.append("			return 'read';                                                  ");
		builder.append("		}                                                                   ");
		builder.append("	}                                                                       ");
		builder.append("}                                                                           ");
		
		Registry registry = createMockRegistry(builder.toString());
		wrapper = new RouteRegistryWrapper(registry);
		
		//return read
		List<ServiceProvider> outputProviders = wrapper
				.getProviders(new Invoke(interfaceName, version, group,
						"getXXX", null, new Object[0], new Class<?>[0]));

		String[] expectedAddresses = new String[] { "10.10.1.0:8686","10.10.2.0:8686" };

		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
		//return write
		outputProviders = wrapper.getProviders(new Invoke(interfaceName,
				version, group, "hello", null, new Object[]{new Men("aaa", new Men("bbb", null))}, new Class<?>[]{Men.class}));

		assertArrayEquals(toAddressArray(providers), toAddressArray(outputProviders));
		
	}
	
	@Test
	public void testAllRule() {
		StringBuilder builder = new StringBuilder();
		builder.append("{                                                                           ");
		builder.append("	defineRouteTables : function () {                                       ");
		builder.append("		return { service:['10.10.1.1'],                                   ");
		builder.append("			read : ['10.10.1.*', '10.10.2.0'],                              ");
		builder.append("			write : ['10.10.2.*']                                           ");
		builder.append("		}                                                                   ");
		builder.append("	},                                                                      ");
		builder.append("	selectRouteTable :function(methodName,parameterTypeNames,args) {     ");
		builder.append("		if(args[0].name && args[0].name == 'aaa') {             			");
		builder.append("			return 'write';                                                 ");
		builder.append("		}                                                                   ");
		builder.append("		if(methodName.indexOf('get') >= 0) {                                ");
		builder.append("			return 'read';                                                  ");
		builder.append("		}                                                                   ");
		builder.append("	 	return 'service';                                                   ");
		builder.append("	}                                                                       ");
		builder.append("}                                                                           ");

		Registry registry = createMockRegistry(builder.toString());
		wrapper = new RouteRegistryWrapper(registry);
		
		
		//return service
		List<ServiceProvider> outputProviders = wrapper
				.getProviders(new Invoke(interfaceName, version, group,
						"match", null,new Object[]{new Object()}, new Class<?>[]{Object.class}));

		String[] expectedAddresses = new String[] {"10.10.1.1:8686"};

		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
		
		
		//return read
	    outputProviders = wrapper
				.getProviders(new Invoke(interfaceName, version, group,
						"get", null, new Object[]{new Object()}, new Class<?>[]{Object.class}));

		expectedAddresses = new String[] {"10.10.1.0:8686","10.10.1.1:8686","10.10.2.0:8686"};

		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
		//return write
	    outputProviders = wrapper
				.getProviders(new Invoke(interfaceName, version, group,
						"get", null, new Object[]{new Men("aaa", null)}, new Class<?>[]{Men.class}));

		expectedAddresses = new String[] {"10.10.2.0:8686","10.10.2.1:8686"};

		assertArrayEquals(expectedAddresses, toAddressArray(outputProviders));
		
		
	}
	
	public static class Men {
		
		public Men(String name, Men son) {
			this.name = name;
			this.son = son;
		}
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Men getSon() {
			return son;
		}

		public void setSon(Men son) {
			this.son = son;
		}

		private String name;
		
		private Men son;
	}
	
	
	
	private Registry createMockRegistry(String jsRule) {
		Registry registry =  createMock(Registry.class);
		registry.addListener(anyObject(RouteRegistryWrapper.class));
		expect(registry.getProviders(anyObject(Invoke.class))).andReturn(providers);

		expect(registry.getServiceConfig(serviceName, configFileName)).andReturn(jsRule);

		replay(registry);
		return registry;
	}
	
	public String[] toAddressArray(List<ServiceProvider> providers) {
		String[] array = new String[providers.size()];
		for (int i = 0; i < providers.size(); i++) {
			array[i] = providers.get(i).getAddress();
		}
		return array;
	}
}
