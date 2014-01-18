package com.github.jremoting.core.test;

import org.junit.Assert;
import org.junit.Test;

import com.github.jremoting.core.DefaultInvokePipeLine;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvokeChain;
import com.github.jremoting.core.InvokeFilter;



public class DefaultInvokePipelineTest {
	
	@Test
	public void testPipeLine() {
		
		final String[] invokes = new String[2];
		Invocation invocation = new Invocation() {
			@Override
			public Object[] getArgs() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Class<?>[] getParameterTypes() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Class<?> getReturnType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getServiceName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getServiceVersion() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getMethodName() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		InvokeFilter first = new InvokeFilter() {
			
			@Override
			public Object invoke(Invocation invocation, InvokeChain chain) {
				invokes[0] = "first";
				return chain.invoke(invocation);
			}
		};
		InvokeFilter last = new InvokeFilter() {
			@Override
			public Object invoke(Invocation invocation, InvokeChain chain) {
				invokes[1] = "last";
				return "";
			}
		};
		
		 DefaultInvokePipeLine pipeLine = new  DefaultInvokePipeLine(new InvokeFilter[]{first, last});
		 
		 Object result = pipeLine.invoke(invocation);
		 
		 Assert.assertEquals("first", invokes[0]);
		 Assert.assertEquals("last", invokes[1]);
		 
		 Assert.assertEquals("", result);
		 
	}

}
