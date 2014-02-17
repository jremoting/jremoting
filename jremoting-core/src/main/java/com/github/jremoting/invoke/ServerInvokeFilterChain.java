package com.github.jremoting.invoke;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.util.ReflectionUtil;
import com.github.jremoting.util.concurrent.ListenableFuture;


public class ServerInvokeFilterChain extends AbstractInvokeFilter   {
	
	private final InvokeFilter head;
	private final InvokeFilter tail;
	public ServerInvokeFilterChain(List<InvokeFilter> invokeFilters) {
		List<InvokeFilter> filters = new ArrayList<InvokeFilter>(invokeFilters.size() + 2);
		this.head = new ServerHeadInvokeFilter();
		filters.add(head);
		
		for (InvokeFilter invokeFilter : invokeFilters) {
			filters.add(invokeFilter);
		}
		this.tail = new ServerTailInvokeFilter();
		filters.add(tail);
		
		InvokeFilterUtil.link(filters);
	}
	
	@Override
	public Object invoke(Invoke invoke) {
		return this.head.invoke(invoke);
	}
	
	@Override
	public ListenableFuture<Object> beginInvoke(Invoke invoke) {
		return this.head.beginInvoke(invoke);
	}

	@Override
	public void endInvoke(Invoke invoke, Object result) {
		this.tail.endInvoke(invoke, result);
	}
	
	
	private static class ServerHeadInvokeFilter extends AbstractInvokeFilter {
		@Override
		public void endInvoke(Invoke invoke, Object result) {
			invoke.getResultFuture().setResult(result);
		}
	}
	
	private static class ServerTailInvokeFilter extends AbstractInvokeFilter {
		
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
	}
}
