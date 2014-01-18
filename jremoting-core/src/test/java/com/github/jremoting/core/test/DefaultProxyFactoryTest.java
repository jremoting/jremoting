package com.github.jremoting.core.test;

import junit.framework.Assert;

import org.junit.Test;

import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvokePipeline;
import com.github.jremoting.core.JdkProxyFactory;

public class DefaultProxyFactoryTest {
	
	@Test
	public void testGetProxy() {
		JdkProxyFactory proxyFactory = new JdkProxyFactory();
		
		InvokePipeline pipeline = new InvokePipeline() {
			
			@Override
			public Object invoke(Invocation invocation) {
				return "hello" + invocation.getArgs()[0] ;
			}
		};
		
	   TestService serviceProxy	= (TestService) proxyFactory.getProxy(TestService.class, pipeline);
	   
	   String result =  serviceProxy.hello("xhan");
	   
	   Assert.assertEquals("helloxhan", result);
	}
}
