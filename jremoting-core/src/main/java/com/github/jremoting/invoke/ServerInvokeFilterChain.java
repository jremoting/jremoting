package com.github.jremoting.invoke;

import java.lang.reflect.Method;
import java.util.List;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.InvokeFilterUtil;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.util.ReflectionUtil;


public class ServerInvokeFilterChain    {
	
	private final InvokeFilter head;

	public ServerInvokeFilterChain(List<InvokeFilter> invokeFilters) {
		invokeFilters.add(new ServerTailInvokeFilter());
		this.head = InvokeFilterUtil.link(invokeFilters);
	}
	
	public  Object invoke(Invoke invoke) {
		return this.head.invoke(invoke);
	}
	
	private  class ServerTailInvokeFilter implements InvokeFilter {
		
		@Override
		public Object invoke(Invoke invoke) {
			try {

				Method targetMethod = ReflectionUtil.findMethod(invoke.getTarget().getClass(), 
						invoke.getMethodName(),
						invoke.getParameterTypes());
				
			    if(targetMethod == null) {
			    	throw new RemotingException("can not find method!");
			    }
			    
			    return targetMethod.invoke(invoke.getTarget(), invoke.getArgs());
			
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public InvokeFilter getNext() {
			return null;
		}
		@Override
		public void setNext(InvokeFilter next) {
		}
	}
}
