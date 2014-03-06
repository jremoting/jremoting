package com.github.jremoting.invoke;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.MessageChannel;
import com.github.jremoting.exception.RemotingException;

public class ClientInvokeFilterChain extends AbstractInvokeFilter {

	private final InvokeFilter head;
	
	private final InvokeFilter tail;
	
	public ClientInvokeFilterChain(MessageChannel messageChannel, List<InvokeFilter> invokeFilters) {
		
		List<InvokeFilter> filters = new ArrayList<InvokeFilter>(invokeFilters.size() + 2);
		this.head = new ClientHeadInvokeFilter();
		filters.add(head);
		
		for (InvokeFilter invokeFilter : invokeFilters) {
			filters.add(invokeFilter);
		}
		this.tail = new ClientTailInvokeFilter(messageChannel);
		filters.add(this.tail);
		
		InvokeFilterUtil.link(filters);
	}
	@Override
	public Object invoke(Invoke invoke) {
		return this.head.invoke(invoke);
	}
	
	@Override
	public void beginInvoke(Invoke invoke) {
		invoke.setInvokeChain(this);
	    this.head.beginInvoke(invoke);
	}

	@Override
	public void endInvoke(Invoke invoke, Object result) {
		this.tail.endInvoke(invoke, result);
	}
	
	public static class ClientHeadInvokeFilter extends AbstractInvokeFilter {
		@Override
		public void endInvoke(Invoke invoke, Object result) {
			invoke.getResultFuture().setResult(result);
		}
	}
	private static class ClientTailInvokeFilter extends AbstractInvokeFilter {
				
		public ClientTailInvokeFilter(MessageChannel messageChannel) {
			this.messageChannel = messageChannel;
		}
		private final MessageChannel messageChannel;
		@Override
		public Object invoke(Invoke invoke) {
			
			messageChannel.send(invoke);
			
			try {
				if(invoke.isTwoWay()) {
					return invoke.getResultFuture().get(invoke.getTimeout(), TimeUnit.MILLISECONDS);
				}
				else {
					return null;
				}
			} catch (InterruptedException e) {
				throw new RemotingException(e);
			} catch (ExecutionException e) {
				throw new RemotingException(e);
			} catch (TimeoutException e) {
				throw new com.github.jremoting.exception.TimeoutException("invoke time out timeout:" + invoke.getTimeout());
			}
		}
		
		@Override
		public void beginInvoke(Invoke invoke) {
			messageChannel.send(invoke);
		}
	}
}
