package com.github.jremoting.invoke;

import java.util.ArrayList;
import java.util.List;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
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
			System.out.println("end of server async end invoke filter");
		}
	}
	
	private static class ServerTailInvokeFilter extends AbstractInvokeFilter {
		
		@Override
		public Object invoke(Invoke invoke) {
			try {
			    return invoke.getTargetMethod().invoke(invoke.getTarget(), invoke.getArgs());
			} catch (Exception e) {
				throw new RuntimeException(e.getCause());
			}
		}
		@SuppressWarnings("unchecked")
		@Override
		public ListenableFuture<Object> beginInvoke(Invoke invoke) {
			try {
			    return (ListenableFuture<Object>)invoke.getTargetMethod().invoke(invoke.getTarget(), invoke.getArgs());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
