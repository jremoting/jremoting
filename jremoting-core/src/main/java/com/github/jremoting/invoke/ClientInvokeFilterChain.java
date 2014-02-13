package com.github.jremoting.invoke;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.InvokeFilterUtil;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.core.MessageChannel;
import com.github.jremoting.exception.RemotingException;

public class ClientInvokeFilterChain {

	private final InvokeFilter head;
	
	public ClientInvokeFilterChain(MessageChannel messageChannel, List<InvokeFilter> invokeFilters) {
		invokeFilters.add(new ClientTailInvokeFilter(messageChannel));
		this.head = InvokeFilterUtil.link(invokeFilters);
	}

	public Object invoke(Invoke invoke) {
		return this.head.invoke(invoke);
	}
	
	private static class ClientTailInvokeFilter implements InvokeFilter {
		
		private static final long DEFAULT_TIMEOUT = 60*1000*5; //default timeout 5 mins
		
		public ClientTailInvokeFilter(MessageChannel messageChannel) {
			this.messageChannel = messageChannel;
		}
		private final MessageChannel messageChannel;
		@Override
		public Object invoke(Invoke invoke) {
			if(!invoke.isTwoWay()) {
				messageChannel.send(invoke);
				return null;
			}
			
			
			MessageFuture future = messageChannel.send(invoke);
			try {
				if(invoke.getTimeout() <= 0) {
					invoke.setTimeout(DEFAULT_TIMEOUT);
				}
				return future.get(invoke.getTimeout(), TimeUnit.MILLISECONDS);
				
			} catch (InterruptedException e) {
				throw new RemotingException(e);
			} catch (ExecutionException e) {
				throw new RemotingException(e);
			} catch (TimeoutException e) {
				throw new com.github.jremoting.exception.TimeoutException("invoke time out timeout:" + invoke.getTimeout());
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
