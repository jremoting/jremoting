package com.github.jremoting.weight.test;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Registry;
import com.github.jremoting.weight.WeightRegistryWrapper;



@RunWith(EasyMockRunner.class)
public class WeightRegistryWrapperTest {
	
	@Mock
    Registry registry;
	
	WeightRegistryWrapper wrapper;
	
	
	@Test
	public void test() {
	
		registry.addListener(anyObject(WeightRegistryWrapper.class));
		expect(registry.getProviders(anyObject(Invoke.class))).andReturn(null);
		replay(registry);
		wrapper = new WeightRegistryWrapper(registry);
		
		wrapper.getProviders(null);
	}
	
	
}
